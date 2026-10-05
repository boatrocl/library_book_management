package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.common.LoanPolicyUtil;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.dto.request.BorrowRequest;
import com.libraflow.library.dto.response.LoanResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.LoanMapper;
import com.libraflow.library.pattern.chain.BorrowContext;
import com.libraflow.library.pattern.chain.BorrowRule;
import com.libraflow.library.pattern.observer.BookReturnedEvent;
import com.libraflow.library.pattern.state.LoanState;
import com.libraflow.library.pattern.state.LoanStateFactory;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.LoanRepository;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.LoanService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service Implementation สำหรับงาน Circulation (ยืม-คืน)
 *
 * SOLID Principles:
 * - SRP: ควบคุม Business Logic การยืม-คืน ประสานงาน Chain of Responsibility, State และ Observer
 * - OCP: ตรวจสอบสิทธิ์ผ่าน List<BorrowRule> และ State ผ่าน LoanStateFactory โดยไม่ต้องแก้ if-else
 * - DIP: Dependency Injection ผ่าน constructor ด้วย private final fields ของ interface ทั้งหมด
 */
@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final BookCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final List<BorrowRule> borrowRules;
    private final LoanStateFactory stateFactory;
    private final ApplicationEventPublisher publisher;
    private final LoanMapper mapper;

    public LoanServiceImpl(LoanRepository loanRepository,
                           BookCopyRepository copyRepository,
                           UserRepository userRepository,
                           List<BorrowRule> borrowRules,
                           LoanStateFactory stateFactory,
                           ApplicationEventPublisher publisher,
                           LoanMapper mapper) {
        this.loanRepository = loanRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.borrowRules = borrowRules;
        this.stateFactory = stateFactory;
        this.publisher = publisher;
        this.mapper = mapper;
    }

    @Override
    public LoanResponse borrow(BorrowRequest request) {
        User member = userRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบข้อมูลสมาชิก id: " + request.memberId()));

        List<BookCopy> copies = copyRepository.findByBarcodeIn(request.barcodes());
        if (copies.size() != request.barcodes().size()) {
            Set<String> foundBarcodes = copies.stream().map(BookCopy::getBarcode).collect(Collectors.toSet());
            List<String> missing = request.barcodes().stream()
                    .filter(b -> !foundBarcodes.contains(b))
                    .toList();
            throw new ResourceNotFoundException("ไม่พบบาร์โค้ดหนังสือในระบบ: " + String.join(", ", missing));
        }

        MemberTier tier = LoanPolicyUtil.resolveMemberTier(member);
        long activeLoanCount = loanRepository.countActiveLoanItemsByUserId(member.getId());

        BorrowContext ctx = new BorrowContext(member, tier, copies, activeLoanCount, BigDecimal.ZERO);

        // ตรวจสอบสิทธิ์ผ่าน Chain of Responsibility ตามลำดับ order() (BR-01 -> BR-02 -> BR-03 -> BR-04)
        borrowRules.stream()
                .sorted(Comparator.comparingInt(BorrowRule::order))
                .forEach(rule -> rule.check(ctx));

        // คำนวณวันกำหนดคืนตามประเภทสมาชิก (BR-05) ผ่าน LoanPolicyUtil
        LocalDate dueDate = LoanPolicyUtil.calculateDueDate(tier);

        String loanCode = generateLoanCode();
        Loan loan = new Loan(loanCode, member, null, LocalDateTime.now(), LoanStatus.ACTIVE);

        for (BookCopy copy : copies) {
            LoanItem item = new LoanItem(loan, copy, dueDate);
            loan.addItem(item);
            copy.setStatus(BookCopyStatus.ON_LOAN);
        }

        Loan saved = loanRepository.save(loan);
        return mapper.toResponse(saved, member.getUsername(), tier.name());
    }

    @Override
    public LoanResponse returnBook(Long loanId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบใบยืม id: " + loanId));

        LoanState state = stateFactory.stateOf(loan);

        for (LoanItem item : loan.getItems()) {
            if (!item.isReturned()) {
                state.onReturn(loan, item);
                BookCopy copy = item.getBookCopy();
                copy.setStatus(BookCopyStatus.AVAILABLE);

                // ยิง Event แจ้งเตือน Observer (BR-10)
                publisher.publishEvent(new BookReturnedEvent(
                        this,
                        copy.getBook().getId(),
                        copy.getId(),
                        loan.getUser().getId()
                ));
            }
        }

        Loan saved = loanRepository.save(loan);
        return mapper.toResponse(saved);
    }

    @Override
    public LoanResponse renewLoan(Long loanId) {
        Loan loan = loanRepository.findByIdWithDetails(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบใบยืม id: " + loanId));

        LoanState state = stateFactory.stateOf(loan);
        state.onRenew(loan);

        Loan saved = loanRepository.save(loan);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบใบยืม id: " + id));
        return mapper.toResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> getAllLoans(LoanStatus status, Pageable pageable) {
        Page<Loan> page = loanRepository.findAllByStatus(status, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> getMemberLoans(Long memberId, Pageable pageable) {
        if (!userRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("ไม่พบข้อมูลสมาชิก id: " + memberId);
        }
        Page<Loan> page = loanRepository.findByUserId(memberId, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    @Override
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบใบยืม id: " + id));
        loanRepository.delete(loan);
    }


    private String generateLoanCode() {
        String datePrefix = "LN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        for (int i = 0; i < 10; i++) {
            String code = datePrefix + String.format("%04d", (int) (Math.random() * 9000) + 1000);
            if (!loanRepository.existsByLoanCode(code)) {
                return code;
            }
        }
        return datePrefix + (System.currentTimeMillis() % 10000);
    }
}
