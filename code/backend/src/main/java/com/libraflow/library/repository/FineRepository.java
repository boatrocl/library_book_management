package com.libraflow.library.repository;

import com.libraflow.library.domain.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    Optional<Fine> findByLoanItemId(Long loanItemId);
}
