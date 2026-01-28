package ru.yandex.practicum.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDto {

    @NotNull
    UUID paymentId;

    @NotNull
    @PositiveOrZero
    Double totalPrice;

    @NotNull
    @PositiveOrZero
    Double deliveryPrice;

    @NotNull
    @PositiveOrZero
    Double feePrice;
}
