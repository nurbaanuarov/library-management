package com.library.management.service.impl;

import com.library.management.dao.AuthorDAO;
import com.library.management.dao.BookCopyDAO;
import com.library.management.dao.BookDAO;
import com.library.management.exception.BookNotFoundException;
import com.library.management.model.Book;
import com.library.management.model.CopyStatus;
import com.library.management.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookCopyDAO copyDao;
    private final BookDAO bookDAO;
    private final AuthorDAO authorDAO;

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookDAO.findAll().stream()
                .map(addAuthorName())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookDAO.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAvailableCopies(Long bookId) {
        return copyDao.findByBookId(bookId).stream()
                .anyMatch(c -> c.getStatus() == CopyStatus.AVAILABLE);
    }

    @Override
    public void save(Book book) {
        bookDAO.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookDAO.deleteById(id);
    }

    private Function<Book,Book> addAuthorName() {
        return book -> {
            authorDAO.findById(book.getAuthor().getId()).ifPresent(book::setAuthor);
            return book;
        };
    }
}
