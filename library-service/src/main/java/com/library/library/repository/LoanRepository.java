package com.library.library.repository;

import com.library.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoanRepository extends JpaRepository<Loan, Integer> {
    Page<Loan> findByUserEmail(String userEmail, Pageable pageable);

    List<Loan> findByReturnedFalse();

    boolean existsByBookIdAndUserEmailAndReturnedFalse(Integer bookId, String userEmail);

}
