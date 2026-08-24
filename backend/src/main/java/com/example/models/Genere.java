package com.example.models;

import jakarta.persistence.*;

@Entity
@Table(name = "genere")
public class Genere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Genere_id")
    private int genereId;

    @Column(name = "Genere_Desc", length = 50, nullable = false, unique = true)
    private String genereDesc;

    public Genere() {
    }

    public int getGenereId() {
        return genereId;
    }

    public void setGenereId(int genereId) {
        this.genereId = genereId;
    }

    public String getGenereDesc() {
        return genereDesc;
    }

    public void setGenereDesc(String genereDesc) {
        this.genereDesc = genereDesc;
    }
}
