package com.library.management.service;

public interface LibrarianService {
    void issueInLibrary(Long userId, Long copyId);
    void giveReserved(Long copyId);
    void cancelReservation(Long copyId);
    void returnCopy(Long copyId);
}
