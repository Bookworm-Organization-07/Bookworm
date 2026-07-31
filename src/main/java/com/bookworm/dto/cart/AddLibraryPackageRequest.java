package com.bookworm.dto.cart;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddLibraryPackageRequest {

    @NotNull
    private Integer libraryPackageId;
}
