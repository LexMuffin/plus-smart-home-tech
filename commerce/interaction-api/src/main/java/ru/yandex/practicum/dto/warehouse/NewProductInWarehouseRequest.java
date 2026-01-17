package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class NewProductInWarehouseRequest {

    @NotNull
    UUID productId;

    @NotNull
    Boolean fragile;

    @NotNull
    DimensionDto dimension;

    @Min(1)
    Double weight;
}
