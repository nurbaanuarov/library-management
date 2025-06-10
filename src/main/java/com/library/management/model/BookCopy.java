package com.library.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCopy {
    private Long id;
    private Book book;
    private String inventoryNumber;
    private CopyStatus status;
}