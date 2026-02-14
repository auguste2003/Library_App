package com.library.library.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Schema(description = "Book entity representing a library book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the book", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Column(nullable = false)
    @Schema(description = "Title of the book", example = "Clean Code")
    private String title;

    @Column(nullable = false)
    @Schema(description = "Author of the book", example = "Robert C. Martin")
    private String author;

    @Column(nullable = false, unique = true)
    @Schema(description = "ISBN number of the book", example = "978-0132350884")
    private String isbn;

    @Schema(description = "Availability status of the book", example = "true", defaultValue = "true")
    private boolean available = true;
}
