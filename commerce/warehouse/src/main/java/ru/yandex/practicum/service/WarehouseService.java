package ru.yandex.practicum.service;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    void addNewProduct(NewProductInWarehouseRequest request);

    void increaseProductQuantity(AddProductToWarehouseRequest request);

    BookedProductsDto checkAvailability(ShoppingCartDto shoppingCart);

    AddressDto getWarehouseAddress();

    BookedProductsDto assemblingProductsForOrder(AssemblyProductsForOrderRequest assemblyRequest);

    void returnProductsToWarehouse(Map<UUID, Integer> returnedProducts);

    void shippedProductsToWarehouse(ShippedToDeliveryRequest deliveryRequest);
}
