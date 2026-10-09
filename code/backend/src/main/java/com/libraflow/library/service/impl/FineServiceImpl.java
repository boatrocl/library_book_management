package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.domain.entity.BookCopy;
import com.libraflow.library.domain.entity.Book;
import com.libraflow.library.domain.enums.MemberTier;
import com.libraflow.library.repository.FineRepository;
import com.libraflow.library.service.FineService;
import com.libraflow.library.service.strategy.FineCalculationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final Map<MemberTier, FineCalculationStrategy> strategies;

    // Dependency Inversion: ใช้ Constructor Injection ตามกฎ
    public FineServiceImpl(FineRepository fineRepository, List<FineCalculationStrategy> strategyList) {
        this.fineRepository = fineRepository;
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FineCalculationStrategy::getApplicableMemberTier, s -> s));
    }

    @Override
    @Transactional
    public Fine generateFine(LoanItem loanItem, int overdueDays, MemberTier memberTier) {
        MemberTier resolvedTier = memberTier != null ? memberTier : MemberTier.STUDENT;
        FineCalculationStrategy strategy = strategies.get(resolvedTier);
        if (strategy == null) {
            throw new IllegalStateException("No fine calculation strategy configured for " + resolvedTier);
        }

        BigDecimal amount = strategy.calculateFine(overdueDays);

        Fine fine = new Fine(loanItem, amount, overdueDays);
        return fineRepository.save(fine);
    }

    @Override
    @Transactional
    public Fine generateLostBookFine(LoanItem loanItem, int overdueDays) {
        Optional<Fine> existingFine = fineRepository.findByLoanItemId(loanItem.getId());
        if (existingFine.isPresent()) {
            return existingFine.get();
        }

        BookCopy copy = loanItem.getBookCopy();
        Book book = copy != null ? copy.getBook() : null;
        BigDecimal replacementCost = book != null ? book.getPrice() : null;
        if (replacementCost == null || replacementCost.signum() < 0) {
            throw new IllegalStateException("Cannot assess a lost-book fine without a valid book price");
        }

        return fineRepository.save(new Fine(loanItem, replacementCost, overdueDays));
    }

    @Override
    @Transactional
    public Fine payFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with id: " + fineId));

        fine.markAsPaid();
        return fineRepository.save(fine);
    }

    @Override
    public java.util.List<com.libraflow.library.dto.response.FineResponse> getFinesByMemberId(Long memberId) {
        java.util.List<Fine> fines = fineRepository.findByUserId(memberId);
        return fines.stream()
                .map(f -> new com.libraflow.library.dto.response.FineResponse(
                        f.getId(),
                        f.getLoanItem() != null ? f.getLoanItem().getId() : null,
                        f.getAmount(),
                        f.getOverdueDays(),
                        f.getStatus().name(),
                        f.getCreatedAt(),
                        f.getPaidAt()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
