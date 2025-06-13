# Library Management System

A web-based library management application built with Spring MVC, Spring Core, JDBC, and Thymeleaf. Supports three user roles—**Admin**, **Librarian**, and **Reader**—each with their own dashboards and workflows:

* **Admin**

  * Manage users (create, disable/enable, assign roles)
  * Manage authors, genres, and books
* **Librarian**

  * View & paginate book catalog
  * Manage physical copies of books
  * Handle in-library issues, home loans, reservations, returns
* **Reader**

  * Browse & paginate available books
  * Place, view, and cancel requests

Internationalized UI (English 🇬🇧 / Russian 🇷🇺) · Secure login with BCrypt · Robust exception handling · Unit tests for DAO & service layers.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Prerequisites](#prerequisites)
3. [Database Setup](#database-setup)
4. [Build & Run](#build--run)
5. [Configuration](#configuration)
6. [Project Structure](#project-structure)
7. [Features](#features)
8. [Internationalization](#internationalization)
9. [Pagination](#pagination)
10. [Exception Handling](#exception-handling)
11. [Tests](#tests)
12. [License](#license)

---

## Tech Stack

* Java 17
* Spring Core & Spring MVC
* Spring Security
* JDBC (plain `JdbcTemplate`–free)
* Thymeleaf templates
* PostgreSQL (or any JDBC-compatible RDBMS)
* BCrypt password hashing
* JUnit 5 + Mockito for unit testing

---

## Prerequisites

* Java 17 SDK
* Maven 3.x
* PostgreSQL 12+ (or MySQL with minor SQL tweaks)
* Git

---

## Database Setup

1. **Create a database** (e.g. `library_db`).
2. **Run the schema** (`src/main/resources/schema.sql`) to create tables.
3. **Insert sample data** via `src/main/resources/data.sql`.
4. **JDBC URL** in `application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/library_db
   spring.datasource.username=youruser
   spring.datasource.password=yourpass
   spring.datasource.driver-class-name=org.postgresql.Driver
   ```
5. (Optional) Enable SQL logging by setting `spring.jpa.show-sql=true`.

---

## Build & Run

```bash
# clone the repo
git clone https://github.com/yourorg/library-management.git
cd library-management

# build
mvn clean package

# run
java -jar target/library-management-0.1.0.jar
```

App will start at [http://localhost:8080](http://localhost:8080).

**Default users** (from `data.sql`):

* `nurba` / `password123` → **ADMIN**
* `libra` / `password123` → **LIBRARIAN**
* `reader1` / `password123` → **READER**

---

## Configuration

All external settings live in `src/main/resources/application.properties`. You can override via environment variables (`SPRING_DATASOURCE_URL`, etc.).

---

## Project Structure

```
src/
├── main/
│   ├── java/com/library/management
│   │   ├── config       # Spring & Security configuration
│   │   ├── controller   # MVC controllers by role
│   │   ├── dao          # DAO interfaces & JDBC implementations
│   │   ├── exception    # Custom exceptions & global handler
│   │   ├── model        # Domain entities + enums
│   │   ├── service      # Business logic
│   │   └── web          # Pagination helper, DTOs
│   └── resources/
│       ├── messages.properties
│       ├── messages_ru.properties
│       ├── schema.sql
│       ├── data.sql
│       ├── templates/   # Thymeleaf HTML templates
│       └── static/css/  # Stylesheets
└── test/                # JUnit 5 + Mockito tests
```

---

## Features

* **Authentication & Authorization**

  * Spring Security roles: `ADMIN`, `LIBRARIAN`, `READER`
* **Admin dashboard**

  * CRUD users, authors, genres, books
* **Librarian dashboard**

  * Paginated view of all books
  * Manage physical copies
  * Process in-library issues, home loans, reservations, returns
* **Reader dashboard**

  * Paginated available books list
  * Place requests (home or in-library)
  * View & cancel pending requests
* **Validation & Errors**

  * Form validations, custom error pages
* **i18n**

  * English & Russian, switchable via UI
* **Security**

  * BCrypt password hashing
  * CSRF disabled for simplicity (but can be enabled)
* **Pagination**

  * Server-side with limit/offset + page controls

---

## Internationalization

All user-facing text lives in `messages.properties` and `messages_ru.properties`.
Switch language via links:

```html
<a th:href="@{/reader/books(lang='ru')}">Русский</a>
<a th:href="@{/reader/books(lang='en')}">English</a>
```

---

## Pagination

Both **Librarian** and **Reader** book lists use a simple `Page<T>` helper:

* Controller reads `?page=` and `?size=`
* DAO does `LIMIT ? OFFSET ?`
* Thymeleaf renders “« Prev” / page numbers / “Next »”

See `com.library.management.web.Page` and `reader/books.html` / `librarian/books.html` for examples.

---

## Exception Handling

A global `@ControllerAdvice` maps:

* `NotFoundException` → **404 Not Found** page
* `InputValidationException` → **400 Bad Request** page
* `EntityInUseException` → **409 Conflict** page
* `DataAccessException` → **500 Database Error** page
* `IllegalStateException` → **400 Invalid Operation**
* Fallback → **500 Unexpected Error**

All templates live in `src/main/resources/templates/error/`.

---

## Tests

* **DAO layer**: Mockito + JUnit5, mock `DataSource`, `Connection`, `ResultSet`
* **Service layer**: Mockito to stub DAOs, verify exception flows and happy paths

To run tests:

```bash
mvn test
```

Aim: ≥ 50% coverage on DAOs & services.

---

## Design & Quality

* **Patterns**: DAO, Service, Builder (for entities)
* **SOLID / Clean Code**: Single Responsibility, clear package layering
* **Security**: PreparedStatements to prevent SQL injection
* **Logging**: (Optional) add SLF4J `@Slf4j` in services & controllers for fine-grained tracing

---

## License

MIT © Your Name / Your Organization

Feel free to open issues or contribute via pull requests!
