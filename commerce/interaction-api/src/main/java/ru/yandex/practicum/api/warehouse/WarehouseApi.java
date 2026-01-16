package ru.yandex.practicum.api.warehouse;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseApi {

    @PutMapping
    void addProduct(@Valid @RequestBody NewProductInWarehouseRequest product);

    @PostMapping("/check")
    BookedProductsDto checkAvailability(@Valid @RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/add")
    void increaseProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}
