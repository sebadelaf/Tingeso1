package com.Tingeso.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kart")
@Getter
@Setter
@NoArgsConstructor
public class KartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String codigo;
    private String modelo;
    private int estado;

    @ElementCollection
    @CollectionTable(name = "kart_reservas"
            , joinColumns = @JoinColumn(name = "kartid"))
    @Column(name = "reservaid")
    private List<Long> reservas = new ArrayList<>();

    public KartEntity(String codigo,String modelo, int estado) {
        this.codigo = codigo;
        this.estado = estado;
        this.id = id;
        this.modelo = modelo;
    }
}
