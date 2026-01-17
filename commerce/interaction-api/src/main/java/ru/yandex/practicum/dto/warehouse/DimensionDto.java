package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = PRIVATE)
public class DimensionDto {

    @Min(1)
    @NotNull
    Double width;

    @Min(1)
    @NotNull
    Double height;

    @Min(1)
    @NotNull
    Double depth;
}
