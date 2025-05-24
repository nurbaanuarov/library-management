package com.library.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность книги (произведения).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private String description;
    private Author author;
    private Genre genre;
    private int totalCopies;
    private LocalDateTime createdAt;
}