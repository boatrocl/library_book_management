package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    Optional<Fine> findByLoanItemId(Long loanItemId);

    /** ดึงรายการค่าปรับทั้งหมดของสมาชิกคนหนึ่ง ผ่านความสัมพันธ์ Fine -> LoanItem -> Loan -> User */
    @org.springframework.data.jpa.repository.Query("SELECT f FROM Fine f WHERE f.loanItem.loan.user.id = :userId")
    java.util.List<Fine> findByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);
}
