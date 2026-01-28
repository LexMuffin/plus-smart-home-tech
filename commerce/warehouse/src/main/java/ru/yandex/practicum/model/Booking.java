package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(schema = "warehouse", name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {

    @Id
    @Column(name = "booking_id", nullable = false, updatable = false)
    UUID bookingId;

    @Column(name = "order_id", nullable = false, unique = true)
    UUID orderId;

    @Column(name = "delivery_id")
    UUID deliveryId;

    @Column(name = "total_weight", nullable = false)
    Double totalWeight;

    @Column(name = "total_volume", nullable = false)
    Double totalVolume;

    @Column(name = "fragile", nullable = false)
    Boolean fragile;

    @ElementCollection
    @CollectionTable(
            name = "booking_products",
            schema = "warehouse",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    Map<UUID, Long> products = new HashMap<>();
}
