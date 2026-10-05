package com.libraflow.library.service.impl;

import com.libraflow.library.domain.entity.Fine;
import com.libraflow.library.domain.entity.LoanItem;
import com.libraflow.library.repository.FineRepository;
import com.libraflow.library.service.FineService;
import com.libraflow.library.service.strategy.FineCalculationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final Map<String, FineCalculationStrategy> strategies;

    // Dependency Inversion: ใช้ Constructor Injection ตามกฎ
    public FineServiceImpl(FineRepository fineRepository, List<FineCalculationStrategy> strategyList) {
        this.fineRepository = fineRepository;
        // จัดกลุ่ม Strategy ตามชื่อ Role เพื่อให้หยิบไปใช้งานได้ง่าย
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(FineCalculationStrategy::getApplicableUserRole, s -> s));
    }

    @Override
    @Transactional
    public Fine generateFine(LoanItem loanItem, int overdueDays, String userRole) {
        // ค้นหา Strategy ตามประเภทสมาชิก ถ้าไม่มีให้ใช้ของ MEMBER เป็นค่าเริ่มต้น
        FineCalculationStrategy strategy = strategies.getOrDefault(userRole, strategies.get("MEMBER"));

        BigDecimal amount = strategy.calculateFine(overdueDays);

        Fine fine = new Fine(loanItem, amount, overdueDays);
        return fineRepository.save(fine);
    }

    @Override
    @Transactional
    public Fine payFine(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found with id: " + fineId));

        fine.markAsPaid();
        return fineRepository.save(fine);
    }
}
