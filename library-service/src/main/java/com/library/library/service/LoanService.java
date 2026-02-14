package com.library.library.service;

import com.library.library.entity.Loan;
import com.library.library.repository.BookRepository;
import com.library.library.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Loan borrowBook(Integer bookId, String userEmail) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (loanRepository.existsByBookIdAndUserEmailAndReturnedFalse(bookId, userEmail)) {
            throw new RuntimeException("You have already borrowed this book");
        }

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        var loan = Loan.builder()
                .book(book)
                .userEmail(userEmail)
                .borrowDate(LocalDateTime.now())
                .returned(false)
                .build();

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Integer loanId) {
        var loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.isReturned()) {
            throw new RuntimeException("Loan already returned");
        }

        loan.setReturned(true);
        loan.setReturnDate(LocalDateTime.now());

        var book = loan.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public Page<Loan> findAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    public Page<Loan> findUserLoans(String email, Pageable pageable) {
        return loanRepository.findByUserEmail(email, pageable);
    }
}
