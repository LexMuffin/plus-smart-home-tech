package ru.yandex.practicum.dto.shoppingStore;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetProductQuantityStateRequest {

    @NotNull
    UUID productId;

    @NotNull
    QuantityState quantityState;
}
