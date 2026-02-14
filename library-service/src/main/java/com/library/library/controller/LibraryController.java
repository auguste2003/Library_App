package com.library.library.controller;

import com.library.library.entity.Book;
import com.library.library.entity.Loan;
import com.library.library.service.BookService;
import com.library.library.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import java.security.Principal;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "library-controller", description = "Library Management - Books and Loans")
public class LibraryController {

        private final BookService bookService;
        private final LoanService loanService;

        // ==================== BOOK ENDPOINTS ====================

        @GetMapping("/books")
        @Operation(summary = "Get all books", description = "Retrieve a list of all books in the library")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books")
        })
        public ResponseEntity<Page<Book>> findAllBooks(
                        @Parameter(description = "Search query for title or author") @RequestParam(required = false) String query,
                        @Parameter(hidden = true) @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
                return ResponseEntity.ok(bookService.findAll(query, pageable));
        }

        @GetMapping("/books/{id}")
        @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved book"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<Book> findBookById(
                        @Parameter(description = "ID of the book to retrieve", example = "1") @PathVariable Integer id) {
                return ResponseEntity.ok(bookService.findById(id));
        }

        @PostMapping("/books")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create a new book", description = "Add a new book to the library (ADMIN only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Book successfully created"),
                        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
        })
        public ResponseEntity<Book> createBook(@RequestBody Book book) {
                return ResponseEntity.status(HttpStatus.CREATED).body(bookService.save(book));
        }

        @PutMapping("/books/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update a book", description = "Update an existing book's information (ADMIN only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Book successfully updated"),
                        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<Book> updateBook(
                        @Parameter(description = "ID of the book to update", example = "1") @PathVariable Integer id,
                        @RequestBody Book book) {
                // Ensure the ID from path is used
                book.setId(id);
                return ResponseEntity.ok(bookService.save(book));
        }

        @DeleteMapping("/books/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Delete a book", description = "Remove a book from the library (ADMIN only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Book successfully deleted"),
                        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<Void> deleteBook(
                        @Parameter(description = "ID of the book to delete", example = "1") @PathVariable Integer id) {
                bookService.deleteById(id);
                return ResponseEntity.noContent().build();
        }

        // ==================== LOAN ENDPOINTS ====================

        @GetMapping("/loans")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get all loans", description = "Retrieve a list of all loans (ADMIN only)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of loans"),
                        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
        })
        public ResponseEntity<Page<Loan>> findAllLoans(
                        @Parameter(hidden = true) @PageableDefault(size = 10, sort = "borrowDate", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity.ok(loanService.findAllLoans(pageable));
        }

        @GetMapping("/loans/my-loans")
        @Operation(summary = "Get my loans", description = "Retrieve all loans for the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved user's loans"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated")
        })
        public ResponseEntity<Page<Loan>> findMyLoans(
                        Principal principal,
                        @Parameter(hidden = true) @PageableDefault(size = 10, sort = "borrowDate", direction = Sort.Direction.DESC) Pageable pageable) {
                return ResponseEntity.ok(loanService.findUserLoans(principal.getName(), pageable));
        }

        @PostMapping("/loans")
        @Operation(summary = "Borrow a book", description = "Create a new loan to borrow a book")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Book successfully borrowed"),
                        @ApiResponse(responseCode = "400", description = "Book not available"),
                        @ApiResponse(responseCode = "401", description = "User not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        public ResponseEntity<Loan> borrowBook(
                        @Parameter(description = "ID of the book to borrow", example = "1") @RequestParam Integer bookId,
                        Principal principal) {
                // principal.getName() extracts the subject (email) from the JWT
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(loanService.borrowBook(bookId, principal.getName()));
        }

        @PostMapping("/loans/{loanId}/return")
        @Operation(summary = "Return a book", description = "Mark a loan as returned")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Book successfully returned"),
                        @ApiResponse(responseCode = "400", description = "Loan already returned"),
                        @ApiResponse(responseCode = "404", description = "Loan not found")
        })
        public ResponseEntity<Loan> returnBook(
                        @Parameter(description = "ID of the loan to return", example = "1") @PathVariable Integer loanId) {
                return ResponseEntity.ok(loanService.returnBook(loanId));
        }
}
