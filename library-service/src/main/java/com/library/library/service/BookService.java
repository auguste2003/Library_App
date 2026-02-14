package com.library.library.service;

import com.library.library.entity.Book;
import com.library.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;

    public Page<Book> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Book> findAll(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query, pageable);
    }

    public Book findById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book save(Book book) {
        return repository.save(book);
    }

    public void deleteById(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Book not found");
        }
        repository.deleteById(id);
    }
}
