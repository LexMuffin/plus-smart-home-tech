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

    WarehouseProduct toEntity(NewProductInWarehouseRequest request);

    Dimension toDimension(DimensionDto dto);
}