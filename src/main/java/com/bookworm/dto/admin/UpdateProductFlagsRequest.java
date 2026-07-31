package com.bookworm.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductFlagsRequest {

    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean isBestseller;
}
