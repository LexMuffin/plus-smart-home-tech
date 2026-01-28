package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;

@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
public interface DeliveryMapper {

    DeliveryMapper INSTANCE = Mappers.getMapper(DeliveryMapper.class);

    @Mapping(target = "totalWeight", ignore = true)
    @Mapping(target = "totalVolume", ignore = true)
    @Mapping(target = "fragile", ignore = true)
    Delivery toEntity(DeliveryDto dto);

    DeliveryDto toDto(Delivery entity);

    Address toAddress(AddressDto dto);

    AddressDto toAddressDto(Address entity);
}


