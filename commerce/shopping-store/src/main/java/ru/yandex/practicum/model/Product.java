package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductState;
import ru.yandex.practicum.dto.shoppingStore.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "products", schema = "shopping_store")
@Getter
@Setter
@ToString
@Builder
@FieldDefaults(level = PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", updatable = false, nullable = false)
    UUID productId;

    @Column(name = "product_name")
    String productName;

    @Column(nullable = false)
    String description;

    @Column(name = "image_src")
    String imageSrc;

    @Enumerated(EnumType.STRING)
    @Column(name = "quantity_state")
    QuantityState quantityState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_state")
    ProductState productState;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category")
    ProductCategory productCategory;

    @Column(precision = 10, scale = 2)
    BigDecimal price;
}
