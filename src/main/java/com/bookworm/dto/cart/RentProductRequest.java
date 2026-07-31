package com.bookworm.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RentProductRequest {

    @NotNull
    private Integer productId;

    @NotNull
    @Min(1)
    private Integer rentDays;
}
