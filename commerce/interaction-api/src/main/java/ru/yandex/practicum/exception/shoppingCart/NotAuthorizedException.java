package ru.yandex.practicum.exception.shoppingCart;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String message) {
        super(message);
    }
}
