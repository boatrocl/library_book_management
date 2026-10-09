package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Loan;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.entity.Reservation;
import com.libraflow.library.domain.entity.User;
import com.libraflow.library.domain.enums.BookCopyStatus;
import com.libraflow.library.domain.enums.LoanStatus;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.domain.enums.ReservationStatus;
import com.libraflow.library.domain.enums.UserRole;
import com.libraflow.library.dto.request.BorrowRequest;
import com.libraflow.library.dto.request.MemberBorrowRequest;
import com.libraflow.library.dto.response.LoanItemResponse;
import com.libraflow.library.dto.response.LoanResponse;
import com.libraflow.library.dto.response.PageResponse;
import com.libraflow.library.exception.BusinessException;
import com.libraflow.library.exception.ErrorCode;
import com.libraflow.library.exception.ResourceNotFoundException;
import com.libraflow.library.mapper.LoanMapper;
import com.libraflow.library.pattern.chain.BorrowContext;
import com.libraflow.library.pattern.chain.BorrowRule;
import com.libraflow.library.pattern.observer.BookReturnedEvent;
import com.libraflow.library.pattern.state.LoanState;
import com.libraflow.library.pattern.state.LoanStateFactory;
import com.libraflow.library.repository.BookCopyRepository;
import com.libraflow.library.repository.BookRepository;
import com.libraflow.library.repository.LoanRepository;
import com.libraflow.library.repository.ReservationRepository;
import com.libraflow.library.repository.UserRepository;
import com.libraflow.library.service.FineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Test สำหรับ LoanServiceImpl
 * ทดสอบตรรกะการยืม (borrow), คืน (returnBook), ต่ออายุ (renewLoan), และการจัดการใบยืม
 * พร้อมตรวจสอบการเรียกใช้งาน Chain of Responsibility, State Pattern และ Observer Pattern
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoanServiceImpl — Circulation Service Logic Test")
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookCopyRepository copyRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BorrowRule rule1;

    @Mock
    private BorrowRule rule2;

    @Mock
    private LoanStateFactory stateFactory;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private LoanMapper mapper;

    @Mock
    private FineService fineService;

    @Mock
    private User mockUser;

    @Mock
    private BookCopy mockCopy1;

    @Mock
    private BookCopy mockCopy2;

    @Mock
    private Book mockBook;

    @Mock
    private LoanState mockState;

    private LoanServiceImpl loanService;

    @BeforeEach
    void setUp() {
        List<BorrowRule> rules = new ArrayList<>(List.of(rule1, rule2));
        loanService = new LoanServiceImpl(
                loanRepository,
                copyRepository,
                bookRepository,
                userRepository,
                reservationRepository,
                rules,
                stateFactory,
                publisher,
                mapper,
                fineService
        );
    }

    private LoanResponse createDummyResponse(Long id) {
        return new LoanResponse(
                id,
                "LN-20261005-1234",
                "testuser",
                "STUDENT",
                LocalDateTime.now(),
                LoanStatus.ACTIVE,
                Collections.emptyList()
        );
    }

    @Nested
    @DisplayName("borrow(...) — การบันทึกการยืมหนังสือ")
    class BorrowTests {

        @Test
        @DisplayName("ยืมสำเร็จ: ผ่านทุกกฎใน Chain, เปลี่ยนสถานะ BookCopy เป็น ON_LOAN, บันทึก Loan")
        void borrow_success() {
            BorrowRequest request = new BorrowRequest(1L, List.of("BC-001", "BC-002"));

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getUsername()).thenReturn("somchai");
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);

            when(copyRepository.findByBarcodeIn(request.barcodes())).thenReturn(List.of(mockCopy1, mockCopy2));

            when(loanRepository.countActiveLoanItemsByUserId(1L)).thenReturn(0L);

            when(rule1.order()).thenReturn(1);
            when(rule2.order()).thenReturn(2);

            when(loanRepository.existsByLoanCode(anyString())).thenReturn(false);

            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

            LoanResponse dummyResponse = createDummyResponse(100L);
            when(mapper.toResponse(any(Loan.class), eq("somchai"), eq("STUDENT"))).thenReturn(dummyResponse);

            LoanResponse response = loanService.borrow(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(100L);

            verify(rule1).check(any(BorrowContext.class));
            verify(rule2).check(any(BorrowContext.class));

            verify(mockCopy1).setStatus(BookCopyStatus.ON_LOAN);
            verify(mockCopy2).setStatus(BookCopyStatus.ON_LOAN);

            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("เจ้าของคิว READY ยืมตัวเล่มที่กันไว้ได้และปิดรายการจอง")
        void borrow_reservedCopyForReservationOwner_shouldFulfillReservation() {
            BorrowRequest request = new BorrowRequest(1L, List.of("BC-READY"));
            Reservation reservation = new Reservation(mockUser, mockBook);
            reservation.markAsReady(mockCopy1, LocalDateTime.now().plusHours(12));

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getUsername()).thenReturn("somchai");
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);
            when(copyRepository.findByBarcodeIn(request.barcodes())).thenReturn(List.of(mockCopy1));
            when(mockCopy1.getId()).thenReturn(22L);
            when(mockCopy1.getStatus()).thenReturn(BookCopyStatus.RESERVED);
            when(reservationRepository.findByUserIdAndReservedCopyIdAndStatusAndExpiresAtAfter(
                    eq(1L), eq(22L), eq(com.libraflow.library.domain.enums.ReservationStatus.READY), any()))
                    .thenReturn(Optional.of(reservation));
            when(loanRepository.countActiveLoanItemsByUserId(1L)).thenReturn(0L);
            when(rule1.order()).thenReturn(1);
            when(rule2.order()).thenReturn(2);
            when(loanRepository.existsByLoanCode(anyString())).thenReturn(false);
            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(mapper.toResponse(any(Loan.class), eq("somchai"), eq("STUDENT")))
                    .thenReturn(createDummyResponse(101L));

            loanService.borrow(request);

            ArgumentCaptor<BorrowContext> contextCaptor = ArgumentCaptor.forClass(BorrowContext.class);
            verify(rule1).check(contextCaptor.capture());
            assertThat(contextCaptor.getValue().isCopyReservedForMember(22L)).isTrue();
            assertThat(reservation.getStatus()).isEqualTo(com.libraflow.library.domain.enums.ReservationStatus.FULFILLED);
            verify(reservationRepository).save(reservation);
            verify(mockCopy1).setStatus(BookCopyStatus.ON_LOAN);
        }

        @Test
        @DisplayName("ยืมไม่สำเร็จ: ไม่พบข้อมูลสมาชิก โยน ResourceNotFoundException")
        void borrow_memberNotFound_shouldThrowException() {
            BorrowRequest request = new BorrowRequest(999L, List.of("BC-001"));
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.borrow(request)
            );

            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("ยืมไม่สำเร็จ: บาร์โค้ดไม่ครบตามที่ระบุ โยน ResourceNotFoundException")
        void borrow_missingBarcodes_shouldThrowException() {
            BorrowRequest request = new BorrowRequest(1L, List.of("BC-001", "BC-999"));

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(copyRepository.findByBarcodeIn(request.barcodes())).thenReturn(List.of(mockCopy1));
            when(mockCopy1.getBarcode()).thenReturn("BC-001");

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.borrow(request)
            );

            assertThat(ex.getMessage()).contains("BC-999");
            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("ยืมไม่สำเร็จ: กฎข้อหนึ่งใน Chain ไม่ผ่าน ต้องหยุดและไม่บันทึก Loan")
        void borrow_ruleChainFails_shouldPropagateException() {
            BorrowRequest request = new BorrowRequest(1L, List.of("BC-001"));

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);

            when(copyRepository.findByBarcodeIn(request.barcodes())).thenReturn(List.of(mockCopy1));
            when(loanRepository.countActiveLoanItemsByUserId(1L)).thenReturn(5L);

            when(rule1.order()).thenReturn(1);
            when(rule2.order()).thenReturn(2);

            doThrow(new BusinessException(ErrorCode.LOAN_QUOTA_EXCEEDED, "โควต้าเต็ม"))
                    .when(rule1).check(any(BorrowContext.class));

            assertThrows(
                    BusinessException.class,
                    () -> loanService.borrow(request)
            );

            verify(rule2, never()).check(any());
            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("สมาชิกยืมด้วยรหัสหนังสือ: เลือกตัวเล่มว่างที่ล็อกไว้และผ่านกฎยืมเดิม")
        void borrowForMember_success() {
            MemberBorrowRequest request = new MemberBorrowRequest(50L, true);
            when(userRepository.findByUsername("somchai")).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(mockUser.getId()).thenReturn(1L);
            when(mockUser.getUsername()).thenReturn("somchai");
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);
            when(bookRepository.existsById(50L)).thenReturn(true);
            when(copyRepository.findFirstByBookIdAndStatusOrderByBarcodeAsc(50L, BookCopyStatus.AVAILABLE))
                    .thenReturn(Optional.of(mockCopy1));
            when(mockCopy1.getBarcode()).thenReturn("BC-050");
            when(copyRepository.findByBarcodeIn(List.of("BC-050"))).thenReturn(List.of(mockCopy1));
            when(loanRepository.countActiveLoanItemsByUserId(1L)).thenReturn(0L);
            when(rule1.order()).thenReturn(1);
            when(rule2.order()).thenReturn(2);
            when(loanRepository.existsByLoanCode(anyString())).thenReturn(false);
            when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(mapper.toResponse(any(Loan.class), eq("somchai"), eq("STUDENT")))
                    .thenReturn(createDummyResponse(150L));

            LoanResponse response = loanService.borrowForMember("somchai", request);

            assertThat(response.id()).isEqualTo(150L);
            verify(copyRepository).findFirstByBookIdAndStatusOrderByBarcodeAsc(50L, BookCopyStatus.AVAILABLE);
            verify(mockCopy1).setStatus(BookCopyStatus.ON_LOAN);
            verify(rule1).check(any(BorrowContext.class));
            verify(rule2).check(any(BorrowContext.class));
        }

        @Test
        @DisplayName("สมาชิกยืมไม่ได้เมื่อไม่มีตัวเล่มว่าง")
        void borrowForMember_noAvailableCopy_shouldThrowConflict() {
            when(userRepository.findByUsername("somchai")).thenReturn(Optional.of(mockUser));
            when(bookRepository.existsById(50L)).thenReturn(true);
            when(copyRepository.findFirstByBookIdAndStatusOrderByBarcodeAsc(50L, BookCopyStatus.AVAILABLE))
                    .thenReturn(Optional.empty());

            BusinessException exception = assertThrows(
                    BusinessException.class,
                    () -> loanService.borrowForMember("somchai", new MemberBorrowRequest(50L, true))
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COPY_NOT_AVAILABLE);
            verify(loanRepository, never()).save(any());
        }

        @Test
        @DisplayName("สมาชิกยืมไม่ได้เมื่อไม่พบหนังสือ")
        void borrowForMember_bookNotFound_shouldThrowNotFound() {
            when(userRepository.findByUsername("somchai")).thenReturn(Optional.of(mockUser));
            when(bookRepository.existsById(50L)).thenReturn(false);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.borrowForMember("somchai", new MemberBorrowRequest(50L, true))
            );

            verify(copyRepository, never()).findFirstByBookIdAndStatusOrderByBarcodeAsc(any(), any());
        }
    }

    @Nested
    @DisplayName("returnBook(...) — การบันทึกการคืนหนังสือ")
    class ReturnTests {

        @Test
        @DisplayName("คืนสำเร็จ: เรียก state.onReturn(), เปลี่ยนสถานะ BookCopy เป็น AVAILABLE, ยิง BookReturnedEvent")
        void returnBook_success() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            LoanItem item = new LoanItem(loan, mockCopy1, LocalDate.now());
            loan.addItem(item);

            when(loanRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(loan));
            when(stateFactory.stateOf(loan)).thenReturn(mockState);
            when(mockCopy1.getBook()).thenReturn(mockBook);
            when(mockBook.getId()).thenReturn(50L);
            when(mockCopy1.getId()).thenReturn(500L);
            when(mockUser.getId()).thenReturn(1L);

            doAnswer(invocation -> {
                item.setReturnedAt(LocalDate.now());
                loan.setStatus(LoanStatus.RETURNED);
                return null;
            }).when(mockState).onReturn(loan, item);

            when(loanRepository.save(loan)).thenReturn(loan);
            when(mapper.toResponse(loan)).thenReturn(createDummyResponse(10L));

            LoanResponse response = loanService.returnBook(10L);

            assertThat(response).isNotNull();
            verify(mockState).onReturn(loan, item);
            verify(mockCopy1).setStatus(BookCopyStatus.AVAILABLE);

            ArgumentCaptor<BookReturnedEvent> eventCaptor = ArgumentCaptor.forClass(BookReturnedEvent.class);
            verify(publisher).publishEvent(eventCaptor.capture());

            BookReturnedEvent event = eventCaptor.getValue();
            assertThat(event.getBookId()).isEqualTo(50L);
            assertThat(event.getCopyId()).isEqualTo(500L);
            assertThat(event.getMemberId()).isEqualTo(1L);

            verify(loanRepository).save(loan);
            verify(fineService, never()).generateFine(any(), anyInt(), any());
        }

        @Test
        @DisplayName("คืนเกินกำหนด: สร้างค่าปรับตามจำนวนวันที่ช้าและประเภทสมาชิก")
        void returnBook_overdue_shouldGenerateFineForMemberTier() {
            Loan loan = new Loan("LN-002", mockUser, null, LocalDateTime.now(), LoanStatus.OVERDUE);
            LoanItem item = new LoanItem(loan, mockCopy1, LocalDate.now().minusDays(4));
            loan.addItem(item);

            when(loanRepository.findByIdWithDetails(11L)).thenReturn(Optional.of(loan));
            when(stateFactory.stateOf(loan)).thenReturn(mockState);
            when(mockUser.getRole()).thenReturn(UserRole.MEMBER);
            when(mockUser.getMemberTier()).thenReturn("EXTERNAL");
            when(mockCopy1.getBook()).thenReturn(mockBook);
            when(mockBook.getId()).thenReturn(50L);
            when(mockCopy1.getId()).thenReturn(500L);
            when(mockUser.getId()).thenReturn(1L);
            doAnswer(invocation -> {
                item.setReturnedAt(LocalDate.now());
                loan.setStatus(LoanStatus.RETURNED);
                return null;
            }).when(mockState).onReturn(loan, item);
            when(loanRepository.save(loan)).thenReturn(loan);
            when(mapper.toResponse(loan)).thenReturn(createDummyResponse(11L));

            loanService.returnBook(11L);

            verify(fineService).generateFine(item, 4, MemberTier.EXTERNAL);
        }

        @Test
        @DisplayName("คืนไม่สำเร็จ: ไม่พบใบยืม โยน ResourceNotFoundException")
        void returnBook_loanNotFound_shouldThrowException() {
            when(loanRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.returnBook(999L)
            );

            verify(publisher, never()).publishEvent(any());
            verify(loanRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("renewLoan(...) — การต่ออายุใบยืม")
    class RenewTests {

        @Test
        @DisplayName("ต่ออายุสำเร็จ: ดึง State ผ่าน Factory และเรียก state.onRenew(loan)")
        void renewLoan_success() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            when(loanRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(loan));
            when(stateFactory.stateOf(loan)).thenReturn(mockState);
            when(loanRepository.save(loan)).thenReturn(loan);
            when(mapper.toResponse(loan)).thenReturn(createDummyResponse(10L));

            LoanResponse response = loanService.renewLoan(10L);

            assertThat(response).isNotNull();
            verify(mockState).onRenew(loan);
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("ต่ออายุไม่สำเร็จ: ไม่พบใบยืม โยน ResourceNotFoundException")
        void renewLoan_loanNotFound_shouldThrowException() {
            when(loanRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.renewLoan(999L)
            );
        }

        @Test
        @DisplayName("ห้ามต่ออายุเมื่อมีสมาชิกกำลังรอจองหนังสือในใบยืม")
        void renewLoan_blockedByWaitingReservation() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            loan.addItem(new LoanItem(loan, mockCopy1, LocalDate.now().plusDays(7)));
            when(mockCopy1.getBook()).thenReturn(mockBook);
            when(mockBook.getId()).thenReturn(5L);
            when(loanRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(loan));
            when(reservationRepository.existsByBookIdAndStatusIn(5L, List.of(ReservationStatus.WAITING)))
                    .thenReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () -> loanService.renewLoan(10L));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RENEW_BLOCKED_BY_RESERVATION);
            verify(stateFactory, never()).stateOf(any(Loan.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }
    }

    @Nested
    @DisplayName("Query & Delete Operations")
    class QueryAndDeleteTests {

        @Test
        @DisplayName("getLoanById: พบใบยืม คืนค่า LoanResponse")
        void getLoanById_success() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            when(loanRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(loan));
            when(mapper.toResponse(loan)).thenReturn(createDummyResponse(1L));

            LoanResponse response = loanService.getLoanById(1L);
            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getLoanById: ไม่พบใบยืม โยน ResourceNotFoundException")
        void getLoanById_notFound_shouldThrowException() {
            when(loanRepository.findByIdWithDetails(1L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.getLoanById(1L)
            );
        }

        @Test
        @DisplayName("getAllLoans: ดึงรายการพร้อม pagination สำเร็จ")
        void getAllLoans_success() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Loan> page = new PageImpl<>(List.of(loan), pageable, 1);

            when(loanRepository.findAllByStatus(LoanStatus.ACTIVE, pageable)).thenReturn(page);
            when(mapper.toResponse(loan)).thenReturn(createDummyResponse(1L));

            PageResponse<LoanResponse> response = loanService.getAllLoans(LoanStatus.ACTIVE, pageable);

            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getMemberLoans: พบสมาชิก คืนค่าประวัติการยืม")
        void getMemberLoans_success() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Loan> page = new PageImpl<>(List.of(loan), pageable, 1);

            when(userRepository.existsById(1L)).thenReturn(true);
            when(loanRepository.findByUserId(1L, pageable)).thenReturn(page);
            when(mapper.toResponse(loan)).thenReturn(createDummyResponse(1L));

            PageResponse<LoanResponse> response = loanService.getMemberLoans(1L, pageable);

            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("getMemberLoans: ไม่พบสมาชิก โยน ResourceNotFoundException")
        void getMemberLoans_memberNotFound_shouldThrowException() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.getMemberLoans(999L, pageable)
            );
        }

        @Test
        @DisplayName("deleteLoan: พบใบยืม ลบสำเร็จ")
        void deleteLoan_success() {
            Loan loan = new Loan("LN-001", mockUser, null, LocalDateTime.now(), LoanStatus.ACTIVE);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            assertDoesNotThrow(() -> loanService.deleteLoan(1L));
            verify(loanRepository).delete(loan);
        }

        @Test
        @DisplayName("deleteLoan: ไม่พบใบยืม โยน ResourceNotFoundException")
        void deleteLoan_notFound_shouldThrowException() {
            when(loanRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> loanService.deleteLoan(1L)
            );
            verify(loanRepository, never()).delete(any());
        }
    }
}
