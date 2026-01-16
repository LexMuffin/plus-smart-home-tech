package ru.yandex.practicum.service;

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
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Продукты должны быть заполнены");
        }
        checkUsername(username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));
        shoppingCart.getProducts().putAll(products);

        shoppingCart = shoppingCartRepository.save(shoppingCart);
        ShoppingCartDto shoppingCartdto = ShoppingCartMapper.INSTANCE.toDto(shoppingCart);
        warehouseApi.checkAvailability(shoppingCartdto);
        return shoppingCartdto;
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
    public ShoppingCartDto removeProductsFromShoppingCart(String username, List<UUID> products) {
        log.debug("Удаление продуктов {} из корзины пользователя {}", products, username);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Корзины пользователя " + username + " не существует"));
        if (shoppingCart.getShopingCartState().equals(ShopingCartState.DEACTIVATED)) {
            throw new IllegalArgumentException("Корзина уже деактивирована");
        }
        if (shoppingCart.getProducts().isEmpty()) {
            throw new NoProductsInShoppingCartException("В корзине отсутствуют продукты для удаления");
        }
        shoppingCartRepository.save(shoppingCart);
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
