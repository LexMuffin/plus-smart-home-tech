package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.dto.warehouse.DimensionDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.model.Dimension;

@Mapper(componentModel = "spring")
public interface WarehouseProductMapper {

    WarehouseProductMapper INSTANCE = Mappers.getMapper(WarehouseProductMapper.class);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "fragile", qualifiedByName = "mapFragile")
    @Mapping(target = "dimension", source = "dimension")
    @Mapping(target = "quantity", constant = "0")
    WarehouseProduct toEntity(NewProductInWarehouseRequest request);

    Dimension toDimension(DimensionDto dto);

    @Named("mapFragile")
    default boolean mapFragile(Boolean fragile) {
        return fragile != null && fragile;
    }
}