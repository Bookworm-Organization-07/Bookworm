package com.example.models;

import jakarta.persistence.*;

@Entity
@Table(name = "product_type_master")
public class Product_Type {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Type_Id")
    private Integer typeId;

    @Column(name = "Type_Desc", length = 50, nullable = false, unique = true)
    private String typeDesc;

    public Product_Type() {
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }
}
