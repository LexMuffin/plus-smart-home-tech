package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.payment.PaymentState;

import java.util.UUID;

@Entity
@Table(schema = "payments", name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", updatable = false, nullable = false)
    UUID paymentId;

    @Column(name = "order_id", nullable = false)
    UUID orderId;

    @Column(name = "product_price")
    Double productPrice;

    @Column(name = "delivery_price")
    Double deliveryPrice;

    @Column(name = "fee_price")
    Double feePrice;

    @Column(name = "total_price")
    Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    PaymentState state;
}
