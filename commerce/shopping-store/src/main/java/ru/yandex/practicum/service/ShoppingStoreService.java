package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;

import java.util.UUID;

public interface ShoppingStoreService {

    ProductDto getProductById(UUID productId);

    Page<ProductDto> getAllProducts(Pageable pageable);

    Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable);

    ProductDto addProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    Boolean updateQuantityState(SetProductQuantityStateRequest request);

    Boolean removeProduct(UUID productId);
}
