package ru.yandex.practicum.dto.shoppingStore;

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
public class ProductDto {

    UUID productId;

    @NotBlank
    String productName;

    @NotBlank
    String description;

    String imageSrc;

    QuantityState quantityState;

    ProductState productState;

    ProductCategory productCategory;

    @NotNull
    Double price;
}
