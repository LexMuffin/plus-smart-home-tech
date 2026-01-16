package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;
import ru.yandex.practicum.service.ShoppingStoreService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreService shoppingStoreService;

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductDto getProductById(@PathVariable @NotNull UUID productId) {
        log.info("GET продукт с ID: {}", productId);
        return shoppingStoreService.getProductById(productId);
    }

    @GetMapping("/products")
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        log.info("GET Все продукты");
        return shoppingStoreService.getAllProducts(pageable);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<ProductDto> getProductsByCategory(
            @RequestParam ProductCategory category,
            Pageable pageable) {
        log.info("GET Продукты по категории: {}", category);
        return shoppingStoreService.getProductsByCategory(category, pageable);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductDto addProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("PUT создание продукта");
        return shoppingStoreService.addProduct(productDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("POST Обновление продукта с ID: {}", productDto.getProductId());
        return shoppingStoreService.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    @ResponseStatus(HttpStatus.OK)
    public boolean removeProduct(@Valid @RequestBody UUID productId) {
        log.info("POST Удаление продукта с ID: {}", productId);
        return shoppingStoreService.removeProduct(productId);
    }

    @PostMapping("/quantityState")
    @ResponseStatus(HttpStatus.OK)
    public boolean updateQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request) {
        log.info("POST Обновление поля по количеству продукта: {}", request.getProductId());
        return shoppingStoreService.updateQuantityState(request);
    }
}
