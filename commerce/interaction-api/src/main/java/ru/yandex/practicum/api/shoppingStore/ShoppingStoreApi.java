package ru.yandex.practicum.api.shoppingStore;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreApi {

    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable("productId") UUID productId);

    @GetMapping("/products")
    Page<ProductDto> getAllProducts(Pageable pageable);

    @GetMapping
    Page<ProductDto> getProductsByCategory(
            @RequestParam("category") ProductCategory category,
            Pageable pageable);

    @PutMapping
    ProductDto addProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    boolean updateQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request);
}
