package com.library.management.exception;

public class EntityInUseException extends RuntimeException {
  public EntityInUseException(String message) {
    super(message);
  }
}
