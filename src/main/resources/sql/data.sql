-- data.sql

-- 1) Roles
INSERT INTO roles (name) VALUES
     ('ADMIN'),
     ('LIBRARIAN'),
     ('READER');

-- 2) Users (bcrypt hashes of 'password123' at cost 12; replace with your own)
INSERT INTO users (username, email, password_hash, enabled) VALUES
('nurba',   'nurba@mail.ru',   '$2a$12$buuy25fxy2osdj01uqjFt.QwdsR7ZoM05a5AEjrNJfXcvr8spY91u', TRUE),
('libra',   'lib@mail.ru',     '$2a$12$N/x7A.aAf9rH/CEJJx.eNeK8JMBijoYZzPpBAMdR3qcC8aUAg/iRG', TRUE),
('reader1', 'reader1@mail.ru', '$2a$12$nWmGVelmo50/cJDeu7bM1OpVypdx5JbKWX1XG1pyJYadUG.k3pzLW', TRUE);

-- 3) Assign Roles to Users
INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1),  -- nurba   → ADMIN
    (2, 2),  -- libra   → LIBRARIAN
    (3, 3);  -- reader1 → READER

-- 4) Authors
INSERT INTO authors (first_name, last_name) VALUES
    ('Jane',    'Austen'),
    ('Charles', 'Dickens'),
    ('Mark',    'Twain');

-- 5) Genres
INSERT INTO genres (name) VALUES
    ('Classic'),
    ('Fiction'),
    ('Adventure');

-- 6) Books
-- (id, title, description, author_id, genre_id, total_copies)
INSERT INTO books (title, description, author_id, genre_id, total_copies) VALUES
    ('Pride and Prejudice', 'A classic novel of manners',             1, 1, 2),
    ('Great Expectations',   'An orphan''s journey of self–discovery', 2, 1, 2),
    ('Adventures of Tom Sawyer', 'Boyhood adventure on the Mississippi', 3, 3, 2);

-- 7) Physical Copies
-- (id, book_id, inventory_number, status)
INSERT INTO book_copies (book_id, inventory_number, status) VALUES
-- Pride and Prejudice copies
    (1, 'PP-001', 'AVAILABLE'),
    (1, 'PP-002', 'AVAILABLE'),
    -- Great Expectations copies
    (2, 'GE-001', 'AVAILABLE'),
    (2, 'GE-002', 'AVAILABLE'),
    -- Tom Sawyer copies
    (3, 'TS-001', 'AVAILABLE'),
    (3, 'TS-002', 'AVAILABLE');

-- -------------------------
-- 8) More Authors
-- -------------------------
INSERT INTO authors (first_name, last_name) VALUES
    ('Fyodor',    'Dostoevsky'),  -- id = 4
    ('Leo',       'Tolstoy'),     -- id = 5
    ('Virginia',  'Woolf'),       -- id = 6
    ('George',    'Orwell'),      -- id = 7
    ('J.K.',      'Rowling');     -- id = 8

-- -------------------------
-- 9) More Genres
-- -------------------------
INSERT INTO genres (name) VALUES
    ('Mystery'),           -- id = 4
    ('Science Fiction'),   -- id = 5
    ('Biography'),         -- id = 6
    ('Poetry'),            -- id = 7
    ('Fantasy');           -- id = 8

-- -------------------------
-- 10) More Books
-- -------------------------
INSERT INTO books (title, description, author_id, genre_id, total_copies) VALUES
    ('Crime and Punishment',
    'A psychological novel set in 19th-c Saint Petersburg',
    4, 4, 3),  -- Dostoevsky, Mystery

    ('War and Peace',
    'Epic tale of Napoleonic wars and Russian society',
    5, 1, 3),  -- Tolstoy, Classic (genre_id=1)

    ('Mrs Dalloway',
    'A single day in the life of Clarissa Dalloway',
    6, 2, 2),  -- Woolf, Fiction (genre_id=2)

    ('1984',
    'Dystopian novel about totalitarian surveillance state',
    7, 5, 2),  -- Orwell, Science Fiction

    ('Animal Farm',
    'Satirical allegory of Soviet politics',
    7, 2, 2),  -- Orwell, Fiction

    ('Harry Potter and the Sorcerer''s Stone',
    'First year at Hogwarts School of Witchcraft & Wizardry',
    8, 8, 3);  -- Rowling, Fantasy

-- -------------------------
-- 11) More Book Copies
-- -------------------------
INSERT INTO book_copies (book_id, inventory_number, status) VALUES
    -- Crime and Punishment copies (book_id=4)
    (4, 'CP-001', 'AVAILABLE'),
    (4, 'CP-002', 'AVAILABLE'),
    (4, 'CP-003', 'AVAILABLE'),

    -- War and Peace copies (book_id=5)
    (5, 'WP-001', 'AVAILABLE'),
    (5, 'WP-002', 'AVAILABLE'),
    (5, 'WP-003', 'AVAILABLE'),

    -- Mrs Dalloway copies (book_id=6)
    (6, 'MD-001', 'AVAILABLE'),
    (6, 'MD-002', 'AVAILABLE'),

    -- 1984 copies (book_id=7)
    (7, '1984-001', 'AVAILABLE'),
    (7, '1984-002', 'AVAILABLE'),

    -- Animal Farm copies (book_id=8)
    (8, 'AF-001', 'AVAILABLE'),
    (8, 'AF-002', 'AVAILABLE'),

    -- Harry Potter copies (book_id=9)
    (9, 'HP-001', 'AVAILABLE'),
    (9, 'HP-002', 'AVAILABLE'),
    (9, 'HP-003', 'AVAILABLE');
