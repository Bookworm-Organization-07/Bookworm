package com.bookworm.dto.library;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LendRequest {

    @NotNull
    private Integer productId;
}
