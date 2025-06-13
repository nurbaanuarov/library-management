package com.library.management.service.impl;

import com.library.management.dao.AuthorDAO;
import com.library.management.dao.BookDAO;
import com.library.management.exception.AuthorNotFoundException;
import com.library.management.exception.EntityInUseException;
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
    private final BookDAO bookDAO;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorDAO.findAll();
    }

    public void save(Author author) {
        authorDAO.save(author);
    }

    @Override
    public void deleteById(Long id) {
        Author author = authorDAO.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException("Author not found with id=" + id));
        long inUse = bookDAO.countByAuthorId(author.getId());
        if (inUse > 0) {
            throw new EntityInUseException(
                    "Cannot delete author “" + author.getFirstName() + " " + author.getLastName() +
                            "” (id=" + author.getId() + "): still referenced by " + inUse + " book(s).");
        }
        authorDAO.delete(author);
    }
}
