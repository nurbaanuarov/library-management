package com.library.management.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    private Long id;
    private User user;
    private BookCopy copy;
    private RequestType type;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime issueDate;
    private LocalDateTime returnDate;
}