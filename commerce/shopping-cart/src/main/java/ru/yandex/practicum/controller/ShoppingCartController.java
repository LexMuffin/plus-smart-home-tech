package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCartDto getShoppingCart(
            @Valid @RequestParam @NotBlank String username) {
        log.info("Получение корзины пользователя: {}", username);
        return shoppingCartService.getShoppingCart(username);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCartDto addProductsToShoppingCart(
            @Valid @NotEmpty @RequestBody Map<@NotNull UUID, @NotNull @Positive Integer> products,
            @Valid @RequestParam @NotBlank String username) {
        log.info("Добавление товаров в корзину {}: {}", username, products);
        return shoppingCartService.addProductsToShoppingCart(username, products);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.OK)
    public void deactivateShoppingCart(
            @Valid @RequestParam @NotBlank String username) {
        log.info("Деактивация корзины: {}", username);
        shoppingCartService.deactivateShoppingCart(username);
    }

    @PostMapping("/remove")
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCartDto removeProductsFromShoppingCart(
            @Valid @RequestParam @NotBlank String username,
            @Valid @NotEmpty @RequestBody List<@NotNull UUID> products) {
        log.info("Удаление товаров из корзины {}: {}", username, products);
        return shoppingCartService.removeProductsFromShoppingCart(username, products);
    }

    @PostMapping("/change-quantity")
    @ResponseStatus(HttpStatus.OK)
    public ShoppingCartDto changeProductQuantity(
            @Valid @RequestParam @NotBlank String username,
            @Valid @RequestBody ChangeProductQuantityRequest request) {
        log.info("Изменение количества товара в корзине {}: {}", username, request);
        return shoppingCartService.changeProductQuantity(username, request);
    }
}