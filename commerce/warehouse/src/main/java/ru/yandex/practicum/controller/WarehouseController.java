package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.warehouse.WarehouseApi;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.service.WarehouseService;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addProduct(@Valid @RequestBody NewProductInWarehouseRequest product) {
        log.debug("Добавление продукта: {}", product);
        warehouseService.addNewProduct(product);
    }

    @Override
    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public BookedProductsDto checkAvailability(ShoppingCartDto shoppingCartDto) {
        log.info("Проверка склада для корзины {}", shoppingCartDto.getShoppingCartId());
        return warehouseService.checkAvailability(shoppingCartDto);
    }

    @Override
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.OK)
    public void increaseProductQuantity(AddProductToWarehouseRequest request) {
        log.debug("Пополнение запасов: {}", request);
        warehouseService.increaseProductQuantity(request);
    }

    @Override
    @GetMapping("/address")
    @ResponseStatus(HttpStatus.OK)
    public AddressDto getWarehouseAddress() {
        log.info("Запрос адреса склада");
        return warehouseService.getWarehouseAddress();
    }
}
