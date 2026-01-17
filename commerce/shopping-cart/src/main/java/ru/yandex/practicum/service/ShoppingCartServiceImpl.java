package ru.yandex.practicum.service;

import feign.FeignException;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.shoppingCart.ShoppingCartApi;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppingCart.ShopingCartState;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.shoppingCart.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.shoppingCart.NotAuthorizedException;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseApi warehouseApi;

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        log.debug("Получение корзины для пользователя {}", username);
        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        return ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto addProductsToShoppingCart(String username, Map<UUID, Integer> products) {
        log.debug("Добавление в корзину продуктов {} для пользователя {}", products, username);

        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));

        // Проверяем, активна ли корзина
        if (shoppingCart.getShopingCartState().equals(ShopingCartState.DEACTIVATED)) {
            throw new IllegalArgumentException("Корзина деактивирована");
        }

        // Инициализируем если null
        if (shoppingCart.getProducts() == null) {
            shoppingCart.setProducts(new HashMap<>());
        }

        if (products == null) {
            products = new HashMap<>();
        }

        // Фильтруем только положительные количества
        Map<UUID, Integer> validProducts = products.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!validProducts.isEmpty()) {
            // Проверяем наличие на складе перед добавлением (как в первой реализации)
            try {
                ShoppingCartDto checkDto = ShoppingCartDto.builder()
                        .shoppingCartId(shoppingCart.getShoppingCartId())
                        .products(validProducts)
                        .build();
                warehouseApi.checkAvailability(checkDto);
            } catch (FeignException e) {
                log.error("Warehouse check failed: {}", e.getMessage());
                throw new IllegalStateException("Не удалось проверить наличие товаров на складе");
            }

            // Добавляем в корзину
            shoppingCart.getProducts().putAll(validProducts);
        }

        shoppingCart = shoppingCartRepository.save(shoppingCart);
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

        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Корзины пользователя " + username + " не существует"));

        if (shoppingCart.getShopingCartState().equals(ShopingCartState.DEACTIVATED)) {
            throw new IllegalArgumentException("Корзина деактивирована");
        }

        if (shoppingCart.getProducts() == null) {
            shoppingCart.setProducts(new HashMap<>());
        }

        if (shoppingCart.getProducts().isEmpty()) {
            throw new NoProductsInShoppingCartException("В корзине отсутствуют продукты для удаления");
        }

        Map<UUID, Integer> cartProducts = shoppingCart.getProducts();
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
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.debug("Изменения количества продуктов для {} у пользовтеля {}", request, username);
        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Корзины пользователя " + username + " не существует"));
        if (shoppingCart.getShopingCartState().equals(ShopingCartState.DEACTIVATED)) {
            throw new IllegalArgumentException("Корзина уже деактивирована");
        }
        shoppingCart.getProducts().put(request.getProductId(), request.getNewQuantity());
        shoppingCart = shoppingCartRepository.save(shoppingCart);
        ShoppingCartDto shoppingCartDto = ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
        warehouseApi.checkAvailability(shoppingCartDto);
        return shoppingCartDto;
    }

    private void checkUsername(String username) {
        log.info("Проверка имени пользователя: {}", username);
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedException("Имя пользователя должно быть заполнено");
        }
    }

    private ShoppingCart createShoppingCart(String username) {
        ShoppingCart newShoppingCart = ShoppingCart.builder()
                .username(username)
                .shopingCartState(ShopingCartState.ACTIVE)
                .products(new HashMap<>())
                .build();
        ShoppingCart createdShoppingCart = shoppingCartRepository.save(newShoppingCart);
        return createdShoppingCart;
    }
}
