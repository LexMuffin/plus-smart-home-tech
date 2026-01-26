package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class BookedProductsDto {

    @Positive
    @NotNull
    Double deliveryWeight;

    @Positive
    @NotNull
    Double deliveryVolume;

    @NotNull
    Boolean fragile;
}
