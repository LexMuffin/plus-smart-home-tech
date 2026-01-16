package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.shoppingStore.*;
import ru.yandex.practicum.exception.shoppingStore.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        log.debug("Получение продукта с ID {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Продукт " + productId + " не найден"));
        return ProductMapper.INSTANCE.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        log.debug("Получение продуктов с параметрами {}", pageable);
        return productRepository.findAll(pageable)
                .map(ProductMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        log.debug("Получение продуктов с категорией {} и параметрами {}", category, pageable);
        return productRepository.findAllByProductCategory(category, pageable)
                .map(ProductMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional
    public ProductDto addProduct(ProductDto productDto) {
        log.debug("Добавление нового продукта");
        if (productDto.getProductId() != null) {
            throw new IllegalArgumentException("product_id должен быть null при создании нового продукта");
        }
        Product product = ProductMapper.INSTANCE.toEntity(productDto);

        Product productToSave = productRepository.save(product);
        log.info("Продукт с ID {} сохранен", productToSave.getProductId());

        return ProductMapper.INSTANCE.toDto(productToSave);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        log.debug("Обновление продукта");
        if (productDto.getProductId() == null || productDto.getProductId().toString().isBlank()) {
            throw new IllegalArgumentException("При обновлении product_id должен быть заполнен");
        }
        log.debug("Обновление продукта с ID {}", productDto.getProductId());

        Product productOld = productRepository.findById(productDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Продукт " + productDto.getProductId() + " не найден"));
        Product productNew = ProductMapper.INSTANCE.toEntity(productDto);
        productNew.setProductId(productOld.getProductId());

        productNew = productRepository.save(productNew);
        log.info("продукта с ID {} обновлен", productNew.getProductId());

        return ProductMapper.INSTANCE.toDto(productNew);

    }

    @Override
    @Transactional
    public Boolean updateQuantityState(SetProductQuantityStateRequest request) {
        log.debug("Обновление поля quantity_state");
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Поле product_id не может быть null");
        }
        log.debug("Обновление поля quantity_state для продукта с ID {}", request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Продукт " + request.getProductId() + " не найден"));
        product.setQuantityState(request.getQuantityState());

        productRepository.save(product);
        log.info("Продукта с ID {} обновлен", product.getProductId());

        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean removeProduct(UUID productId) {
        log.debug("Удаление продукта с ID {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Продукт " + productId + " не найден"));

        product.setProductState(ProductState.DEACTIVE);

        productRepository.save(product);
        log.debug("Продукт с ID {} удален", productId);

        return Boolean.TRUE;
    }
}
