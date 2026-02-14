package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.BaseIntegrationTest;
import com.library.library.entity.Book;
import com.library.library.entity.Loan;
import com.library.library.repository.BookRepository;
import com.library.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class LoanControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    void shouldBorrowBookSuccessfully() throws Exception {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setIsbn("978-0132350884");
        book = bookRepository.save(book);

        String token = generateToken("USER", "user@library.com");

        mockMvc.perform(post("/loans?bookId=" + book.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.id").value(book.getId()))
                .andExpect(jsonPath("$.userEmail").value("user@library.com"));
    }

    @Test
    void shouldFailToBorrowUnavailableBook() throws Exception {
        Book book = new Book();
        book.setTitle("Clean Code");
        book.setAuthor("Robert Martin");
        book.setIsbn("978-0132350884");
        book = bookRepository.save(book);

        // First loan
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUserEmail("other@library.com");
        loan.setBorrowDate(LocalDateTime.now());
        loan.setReturned(false);
        loanRepository.save(loan);

        book.setAvailable(false);
        bookRepository.save(book);

        String token = generateToken("USER", "user@library.com");

        mockMvc.perform(post("/loans?bookId=" + book.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict()); // Expecting 409 Conflict for already borrowed book
    }

    @Test
    void shouldReturnBookSuccessfully() throws Exception {
        Book book = new Book();
        book.setTitle("Refactoring");
        book.setAuthor("Martin Fowler");
        book.setIsbn("12345");
        book = bookRepository.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUserEmail("user@library.com");
        loan.setBorrowDate(LocalDateTime.now());
        loan.setReturned(false);
        loan = loanRepository.save(loan);

        String token = generateToken("USER", "user@library.com");

        mockMvc.perform(post("/loans/" + loan.getId() + "/return")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void shouldGetMyLoans() throws Exception {
        Book b1 = new Book();
        b1.setTitle("B1");
        b1.setAuthor("Author 1");
        b1.setIsbn("1");
        b1 = bookRepository.save(b1);

        Book b2 = new Book();
        b2.setTitle("B2");
        b2.setAuthor("Author 2");
        b2.setIsbn("2");
        b2 = bookRepository.save(b2);

        // My Loan
        Loan l1 = new Loan();
        l1.setBook(b1);
        l1.setUserEmail("me@library.com");
        l1.setBorrowDate(LocalDateTime.now());
        l1.setReturned(false);
        loanRepository.save(l1);

        // Other's Loan
        Loan l2 = new Loan();
        l2.setBook(b2);
        l2.setUserEmail("other@library.com");
        l2.setBorrowDate(LocalDateTime.now());
        l2.setReturned(false);
        loanRepository.save(l2);

        String token = generateToken("USER", "me@library.com");

        mockMvc.perform(get("/loans/my-loans")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].book.id").value(b1.getId()));
    }
}
