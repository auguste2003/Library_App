package com.library.library.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Schema(description = "Loan entity representing a book loan transaction")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the loan", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    @Schema(description = "The book being loaned")
    private Book book;

    @Schema(description = "Email of the user who borrowed the book", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Date and time when the book was borrowed", example = "2026-01-31T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime borrowDate;

    @Schema(description = "Date and time when the book was returned", example = "2026-02-05T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime returnDate;

    @Schema(description = "Whether the book has been returned", example = "false", defaultValue = "false")
    private boolean returned;
}
