package com.bookworm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stakeholder_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StakeholderRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stakeholder_role_id")
    private Integer stakeholderRoleId;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;
}
