package ru.yandex.practicum.api.shoppingCart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartApi {

    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String username);

    @PutMapping
    ShoppingCartDto addProductToShoppingCart(
            @RequestBody @NotEmpty Map<@NotNull UUID, @NotNull @Positive Long> products,
            @RequestParam @NotBlank String username);

    @DeleteMapping
    void deactivateCurrentShoppingCart(@RequestParam @NotBlank String username);

    @PostMapping("/remove")
    ShoppingCartDto removeProductsFromShoppingCart(
            @RequestParam @NotBlank String username,
            @RequestBody List<UUID> products);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeQuantity(
            @RequestParam @NotBlank String username,
            @RequestBody @Valid ChangeProductQuantityRequest request);
}
