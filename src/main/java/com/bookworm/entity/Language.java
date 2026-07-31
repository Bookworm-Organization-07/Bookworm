package com.bookworm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "language_id")
    private Integer languageId;

    @Column(name = "language_code", nullable = false, unique = true, length = 10)
    private String languageCode;

    @Column(name = "language_name", nullable = false, length = 50)
    private String languageName;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
}
