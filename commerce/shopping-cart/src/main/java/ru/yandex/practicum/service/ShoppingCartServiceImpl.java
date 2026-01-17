package ru.yandex.practicum.service;

import feign.FeignException;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShopingCartState;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.exception.shoppingCart.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.shoppingCart.NotAuthorizedException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseApi warehouseApi;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        log.debug("Получение корзины для пользователя {}", username);
        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        return ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductsToShoppingCart(String username, Map<UUID, Integer> products) {
        log.debug("Добавление в корзину продуктов {} для пользователя {}", products, username);

        checkUsername(username);

        if (products == null || products.isEmpty()) {
            log.warn("Попытка добавления пустого списка товаров для пользователя {}", username);
            return getShoppingCart(username);
        }

        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        if (shoppingCart.getShopingCartState() == ShopingCartState.DEACTIVATED) {
            throw new IllegalArgumentException("Корзина деактивирована");
        }

        if (shoppingCart.getProducts() == null) {
            shoppingCart.setProducts(new HashMap<>());
        }

        Map<UUID, Integer> validProducts = products.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!validProducts.isEmpty()) {
            try {
                ShoppingCartDto checkDto = ShoppingCartDto.builder()
                        .shoppingCartId(shoppingCart.getShoppingCartId())
                        .products(validProducts)
                        .build();
                warehouseApi.checkAvailability(checkDto);
            } catch (FeignException e) {
                log.error("Warehouse check failed: {}", e.getMessage());
                log.error("Ошибка при проверке наличия товаров на складе: {}", e.getMessage());

                if (e.status() == 400) {
                    throw new IllegalArgumentException("Товары недоступны в запрашиваемом количестве");
                }

                log.warn("Сервис склада временно недоступен. Товары добавлены в корзину без проверки.");
            }

            Map<UUID, Integer> currentProducts = shoppingCart.getProducts();
            validProducts.forEach((productId, quantity) -> {
                Integer currentQuantity = currentProducts.getOrDefault(productId, 0);
                currentProducts.put(productId, currentQuantity + quantity);
            });

            shoppingCart.setProducts(currentProducts);
            shoppingCart = shoppingCartRepository.save(shoppingCart);
        }

        return ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public void deactivateShoppingCart(String username) {
        log.debug("Деактивация корзины пользователя {}", username);
        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        shoppingCart.setShopingCartState(ShopingCartState.DEACTIVATED);
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeProductsFromShoppingCart(String username, List<UUID> products) {
        log.debug("Удаление продуктов {} из корзины пользователя {}", products, username);

        checkUsername(username);

        if (products == null || products.isEmpty()) {
            log.warn("Попытка удаления пустого списка товаров для пользователя {}", username);
            return getShoppingCart(username);
        }

        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Корзины пользователя " + username + " не существует"));

        if (shoppingCart.getShopingCartState() == ShopingCartState.DEACTIVATED) {
            throw new IllegalArgumentException("Корзина деактивирована");
        }

        if (shoppingCart.getProducts() == null) {
            shoppingCart.setProducts(new HashMap<>());
        }

        Map<UUID, Integer> cartProducts = shoppingCart.getProducts();
        if (cartProducts.isEmpty()) {
            throw new NoProductsInShoppingCartException("В корзине отсутствуют продукты для удаления");
        }

        List<UUID> missingProducts = products.stream()
                .filter(productId -> !cartProducts.containsKey(productId))
                .toList();

        if (!missingProducts.isEmpty()) {
            throw new NoProductsInShoppingCartException(
                    "Следующие продукты не найдены в корзине: " + missingProducts
            );
        }

        Map<UUID, Integer> updatedProducts = new HashMap<>(cartProducts);
        products.forEach(updatedProducts::remove);
        shoppingCart.setProducts(updatedProducts);

        shoppingCart = shoppingCartRepository.save(shoppingCart);
        return ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.debug("Изменения количества продуктов для {} у пользователя {}", request, username);

        checkUsername(username);

        if (request == null) {
            throw new IllegalArgumentException("Запрос не может быть пустым");
        }

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("ID продукта не может быть пустым");
        }

        if (request.getNewQuantity() == null || request.getNewQuantity() < 0) {
            throw new IllegalArgumentException("Новое количество должно быть неотрицательным числом");
        }

        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Корзины пользователя " + username + " не существует"));

        if (shoppingCart.getShopingCartState() == ShopingCartState.DEACTIVATED) {
            throw new IllegalArgumentException("Корзина уже деактивирована");
        }

        if (shoppingCart.getProducts() == null) {
            shoppingCart.setProducts(new HashMap<>());
        }

        if (!shoppingCart.getProducts().containsKey(request.getProductId()) && request.getNewQuantity() > 0) {
            throw new NoProductsInShoppingCartException("Товар не найден в корзине");
        }

        if (request.getNewQuantity() == 0) {
            shoppingCart.getProducts().remove(request.getProductId());
        } else {
            shoppingCart.getProducts().put(request.getProductId(), request.getNewQuantity());
        }

        shoppingCart = shoppingCartRepository.save(shoppingCart);

        if (request.getNewQuantity() > 0) {
            try {
                ShoppingCartDto shoppingCartDto = ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
                warehouseApi.checkAvailability(shoppingCartDto);
            } catch (FeignException e) {
                log.error("Warehouse check failed: {}", e.getMessage());
                if (e.status() == 400) {
                    throw new IllegalArgumentException("Товар недоступен в запрашиваемом количестве");
                }
                log.warn("Сервис склада временно недоступен при изменении количества товара.");
            }
        }

        return ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
    }

    private void checkUsername(String username) {
        log.debug("Проверка имени пользователя: {}", username);
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedException("Имя пользователя должно быть заполнено");
        }
    }

    @Transactional
    private ShoppingCart createShoppingCart(String username) {
        ShoppingCart newShoppingCart = ShoppingCart.builder()
                .username(username)
                .shopingCartState(ShopingCartState.ACTIVE)
                .products(new HashMap<>())
                .build();
        return shoppingCartRepository.save(newShoppingCart);
    }
}