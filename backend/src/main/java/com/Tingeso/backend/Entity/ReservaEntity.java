package com.Tingeso.backend.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
public class ReservaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String fechahora;
    private int tiporeserva;
    private int cantidadpersonas;
    private Long iduser;
    private int cantidadcumple;

    public ReservaEntity(String fechahora,int tiporeserva, int cantidadpersonas,Long iduser, int cantidadcumple) {
        this.fechahora = fechahora;
        this.id = id;
        this.tiporeserva = tiporeserva;
        this.cantidadpersonas = cantidadpersonas;
        this.iduser = iduser;
        this.cantidadcumple = cantidadcumple;
    }

}
