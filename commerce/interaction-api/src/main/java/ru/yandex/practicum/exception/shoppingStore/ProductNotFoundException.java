package ru.yandex.practicum.exception.shoppingStore;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
