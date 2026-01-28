package ru.yandex.practicum.exception.order;

public class NoAuthorizedUserException extends RuntimeException {
    public NoAuthorizedUserException(String message) {
        super(message);
    }
}
