package com.library.management.service.impl;

import com.library.management.dao.BookCopyDAO;
import com.library.management.exception.BookCopyNotFoundException;
import com.library.management.model.BookCopy;
import com.library.management.model.CopyStatus;
import com.library.management.service.LibrarianCopyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LibrarianCopyServiceImpl implements LibrarianCopyService {
    private final BookCopyDAO bookCopyDAO;

    @Override
    @Transactional(readOnly = true)
    public List<BookCopy> findAll() {
        return bookCopyDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCopy> findByBookId(Long bookId) {
        return bookCopyDAO.findByBookId(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public BookCopy findById(Long id) {
        return bookCopyDAO.findById(id)
                .orElseThrow(() -> new BookCopyNotFoundException("Book copy not found with id " + id));
    }

    @Override
    public void create(BookCopy copy) {
        bookCopyDAO.save(copy);
    }

    @Override
    public void changeStatus(Long copyId, CopyStatus newStatus) {
        bookCopyDAO.updateStatus(copyId, newStatus);
    }

    @Override
    public void update(BookCopy copy) {
        bookCopyDAO.update(copy);
    }
}
