package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.order.OrderState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(schema = "orders", name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID orderId;

    @Column(name = "shopping_cart_id")
    UUID shoppingCartId;

    @Column(name = "username", nullable = false)
    String username;

    @ElementCollection
    @CollectionTable(name = "order_products", schema = "orders", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    @Builder.Default
    Map<UUID, Integer> products = new HashMap<>();

    @Column(name = "payment_id")
    UUID paymentId;

    @Column(name = "delivery_id")
    UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    OrderState state;

    @Column(name = "delivery_weight")
    Double deliveryWeight;

    @Column(name = "delivery_volume")
    Double deliveryVolume;

    @Column(name = "fragile")
    Boolean fragile;

    @Column(name = "total_price")
    Double totalPrice;

    @Column(name = "delivery_price")
    Double deliveryPrice;

    @Column(name = "product_price")
    Double productPrice;
}
