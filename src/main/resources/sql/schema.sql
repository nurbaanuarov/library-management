-- 1. Создать БД с UTF8 (если ещё не создана)
CREATE DATABASE library_management
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LOCALE_PROVIDER = 'libc'
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;
\c library_management

-- 2. Таблица ролей
CREATE TABLE roles (
                       id   BIGSERIAL PRIMARY KEY,
                       name VARCHAR(20) NOT NULL UNIQUE  -- e.g., READER, LIBRARIAN, ADMIN
);

-- 3. Пользователи
CREATE TABLE users (
                       id             BIGSERIAL PRIMARY KEY,
                       username       VARCHAR(50) NOT NULL UNIQUE,
                       email          VARCHAR(100) NOT NULL UNIQUE,
                       password_hash  VARCHAR(60) NOT NULL,  -- hash
                       enabled        BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                       last_modified  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- trigger to auto-update last_modified
CREATE OR REPLACE FUNCTION fn_update_last_modified()
    RETURNS trigger AS $$
BEGIN
    NEW.last_modified := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_last_modified
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE PROCEDURE fn_update_last_modified();

-- 4. Связующая таблица для many-to-many User⇄Role
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id)   ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id)   ON DELETE CASCADE
);

-- 5. Авторы
CREATE TABLE authors (
                         id   BIGSERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         surname VARCHAR(100) NOT NULL
);

-- 6. Жанры
CREATE TABLE genres (
                        id   BIGSERIAL PRIMARY KEY,
                        name VARCHAR(50) NOT NULL UNIQUE
);

-- 7. Книги
CREATE TABLE books (
                       id              BIGSERIAL PRIMARY KEY,
                       title           VARCHAR(200) NOT NULL,
                       description     TEXT,
                       author_id       BIGINT NOT NULL,
                       genre_id        BIGINT NOT NULL,
                       total_copies    INTEGER NOT NULL CHECK (total_copies >= 0),
                       created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                       FOREIGN KEY (author_id) REFERENCES authors(id),
                       FOREIGN KEY (genre_id)  REFERENCES genres(id)
);

-- 8. Экземпляры книг
CREATE TYPE copy_status AS ENUM ('AVAILABLE', 'ISSUED', 'RESERVED');

CREATE TABLE book_copies (
                             id               BIGSERIAL PRIMARY KEY,
                             book_id          BIGINT NOT NULL,
                             inventory_number VARCHAR(50) NOT NULL UNIQUE,
                             status           copy_status NOT NULL DEFAULT 'AVAILABLE',
                             FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- 9. Запросы/заказы на выдачу
CREATE TYPE request_type AS ENUM ('HOME', 'IN_LIBRARY');
CREATE TYPE request_status AS ENUM ('PENDING', 'ISSUED', 'RETURNED', 'CANCELED');

CREATE TABLE book_requests (
                               id            BIGSERIAL PRIMARY KEY,
                               user_id       BIGINT NOT NULL,
                               copy_id       BIGINT,  -- присваивается при фактической выдаче
                               type          request_type NOT NULL,
                               status        request_status NOT NULL DEFAULT 'PENDING',
                               request_date  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                               issue_date    TIMESTAMP WITHOUT TIME ZONE,
                               return_date   TIMESTAMP WITHOUT TIME ZONE,
                               FOREIGN KEY (user_id) REFERENCES users(id),
                               FOREIGN KEY (copy_id) REFERENCES book_copies(id)
);

-- 10. Отзывы
CREATE TABLE book_comments (
                               id          BIGSERIAL PRIMARY KEY,
                               user_id     BIGINT NOT NULL,
                               book_id     BIGINT NOT NULL,
                               rating      SMALLINT CHECK (rating BETWEEN 1 AND 5),
                               comment     TEXT,
                               created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
                               FOREIGN KEY (user_id) REFERENCES users(id),
                               FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 11. Индексы для ускорения поиска
CREATE INDEX idx_books_title ON books USING btree (lower(title));
CREATE INDEX idx_books_author ON books USING btree (author_id);
CREATE INDEX idx_requests_user ON book_requests (user_id);
CREATE INDEX idx_copies_status ON book_copies (status);
