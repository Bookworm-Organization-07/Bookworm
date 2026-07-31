package com.bookworm.dto.order;

import com.bookworm.entity.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull
    private PaymentMode paymentMode;

    /** Decorative only in this simulated gateway — no real card is ever charged or stored. */
    private String cardNumber;

    /** Testing aid: force this attempt to fail, so the decline path can be demonstrated on demand. */
    private boolean simulateFailure;
}
