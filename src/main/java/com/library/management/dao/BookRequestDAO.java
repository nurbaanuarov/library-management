package com.library.management.dao;

import com.library.management.model.BookRequest;

public interface BookRequestDAO {
    void save(BookRequest request);
    // Later: findAll, findByUserId, updateStatus, etc.
}
