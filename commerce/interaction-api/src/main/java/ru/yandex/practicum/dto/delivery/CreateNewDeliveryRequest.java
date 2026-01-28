package ru.yandex.practicum.dto.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.warehouse.AddressDto;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateNewDeliveryRequest {

    @NotNull
    UUID orderId;

    @NotNull
    AddressDto toAddress;

    AddressDto fromAddress;

    Double totalWeight;

    Double totalVolume;

    Boolean fragile;
}
