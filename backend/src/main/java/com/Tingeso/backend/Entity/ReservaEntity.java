package com.Tingeso.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
public class ReservaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String fechahora;
    private int tiporeserva;
    private int cantidadpersonas;
    private Long iduser;

    public ReservaEntity(String fechahora,int tiporeserva, int cantidadpersonas,Long iduser) {
        this.fechahora = fechahora;
        this.id = id;
        this.tiporeserva = tiporeserva;
        this.cantidadpersonas = cantidadpersonas;
        this.iduser = iduser;
    }
}
