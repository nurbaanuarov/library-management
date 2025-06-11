package com.library.management.service;

import com.library.management.model.RequestType;

import java.time.LocalDate;

public interface LibrarianService {
    void issueCopyManually(Long userId, Long copyId, RequestType type, LocalDate returnDate);
}
