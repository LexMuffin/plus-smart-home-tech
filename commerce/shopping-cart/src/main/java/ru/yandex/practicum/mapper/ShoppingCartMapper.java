package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.dto.shoppingCart.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ShoppingCartMapper {

    ShoppingCartMapper INSTANCE = Mappers.getMapper(ShoppingCartMapper.class);

    @Mapping(target = "shoppingCartId", source = "shoppingCartId", qualifiedByName = "uuidToString")
    @Mapping(target = "products", source = "products")
    ShoppingCartDto toDto(ShoppingCart shoppingCart);

    @Mapping(target = "shoppingCartId", source = "shoppingCartId", qualifiedByName = "stringToUuid")
    @Mapping(target = "products", source = "products")
    ShoppingCart toEntity(ShoppingCartDto shoppingCartDto);

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    default UUID stringToUuid(String uuid) {
        try {
            return uuid != null ? UUID.fromString(uuid) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
