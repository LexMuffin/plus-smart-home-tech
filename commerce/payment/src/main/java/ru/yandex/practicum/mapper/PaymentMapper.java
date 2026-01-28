package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.model.Payment;

@Mapper(componentModel = org.mapstruct.MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "productPrice", expression = "java(calculateProductPrice(dto))")
    Payment toEntity(PaymentDto dto);

    PaymentDto toDto(Payment entity);

    default Double calculateProductPrice(PaymentDto dto) {
        if (
                dto == null || dto.getTotalPrice() == null ||
                dto.getDeliveryPrice() == null || dto.getFeePrice() == null) {
            return 0.0;
        }
        return dto.getTotalPrice() - dto.getDeliveryPrice() - dto.getFeePrice();
    }
}
