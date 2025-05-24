package com.library.management.exception;

public class DataAccessException extends RuntimeException {
    /**
     * @param message описание ошибки
     * @param cause причина (SQL-исключение)
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message описание ошибки без вложенного исключения
     */
    public DataAccessException(String message) {
        super(message);
    }
}