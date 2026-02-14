package com.library.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.library.BaseIntegrationTest;
import com.library.library.entity.Book;
import com.library.library.repository.BookRepository;
import com.library.library.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class LibraryControllerTest extends BaseIntegrationTest {

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
    void shouldCreateBookAsAdmin() throws Exception {
        Book book = new Book();
        book.setTitle("Domain Driven Design");
        book.setAuthor("Eric Evans");
        book.setIsbn("978-0321125217");

        String token = generateToken("ADMIN", "admin@library.com");

        mockMvc.perform(post("/books") // Direct path, no /api/library prefix in service tests
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Domain Driven Design"));
    }

    @Test
    void shouldReturnForbiddenWhenUserCreatesBook() throws Exception {
        Book book = new Book();
        book.setTitle("Hacking 101");
        book.setAuthor("Anonymous");
        book.setIsbn("123456");

        String token = generateToken("USER", "user@library.com");

        mockMvc.perform(post("/books")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnPaginatedBooks() throws Exception {
        // Insert 15 books
        for (int i = 1; i <= 15; i++) {
            Book book = new Book();
            book.setTitle("Book " + i);
            book.setAuthor("Author " + i);
            book.setIsbn("ISBN-" + i);
            bookRepository.save(book);
        }

        String token = generateToken("USER", "user@library.com");

        mockMvc.perform(get("/books")
                .header("Authorization", "Bearer " + token)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(15));
    }

    @Test
    void shouldSearchBooksByTitle() throws Exception {
        Book b1 = new Book();
        b1.setTitle("Spring Boot in Action");
        b1.setAuthor("Craig Walls");
        b1.setIsbn("111");
        bookRepository.save(b1);

        Book b2 = new Book();
        b2.setTitle("Java Concurrency");
        b2.setAuthor("Brian Goetz");
        b2.setIsbn("222");
        bookRepository.save(b2);

        String token = generateToken("USER", "user@library.com");

        mockMvc.perform(get("/books")
                .header("Authorization", "Bearer " + token)
                .param("query", "Spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot in Action"));
    }
}
