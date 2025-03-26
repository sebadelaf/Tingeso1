package com.Tingeso.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter
@Setter
@NoArgsConstructor
public class AdminEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nombre;
    private String apellidopaterno;
    private String apellidomaterno;
    private String email;
    private String password;

    public AdminEntity(String nombre, String apellidopaterno,String apellidomaterno,  String email,  String password) {
        this.apellidomaterno = apellidomaterno;
        this.apellidopaterno = apellidopaterno;
        this.email = email;
        this.id = id;
        this.nombre = nombre;
        this.password = password;
    }
}
