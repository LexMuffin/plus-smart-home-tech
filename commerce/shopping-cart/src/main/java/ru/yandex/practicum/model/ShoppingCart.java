package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.shoppingCart.ShopingCartState;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts", schema = "carts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shopping_cart_id", updatable = false, nullable = false)
    UUID shoppingCartId;

    String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "shopping_cart_state")
    ShopingCartState shopingCartState;

    @ElementCollection
    @CollectionTable(
            name = "products_in_shopping_carts",
            schema = "carts",
            joinColumns = @JoinColumn(name = "shopping_cart_id")
    )
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    Map<UUID, Integer> products;
}
