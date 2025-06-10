package com.library.management.service.impl;

import com.library.management.dao.AuthorDAO;
import com.library.management.model.Author;
import com.library.management.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorDAO authorDAO;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorDAO.findAll();
    }

    public void save(Author author) {
        authorDAO.save(author);
    }

    public void delete(Author author) {
        authorDAO.delete(author);
    }
}
