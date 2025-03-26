package com.Tingeso.backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nombre;
    private String apellidopaterno;
    private String apellidomaterno;
    private String email;
    private String password;
    @ElementCollection
    @CollectionTable(name = "usuario_reservas"
            , joinColumns = @JoinColumn(name = "userid"))
    @Column(name = "reservaid")
    private List<Long> idreservasuser = new ArrayList<>();

    public UserEntity(String nombre, String apellidopaterno, String apellidomaterno,  String email,   String password) {
        this.apellidomaterno = apellidomaterno;
        this.apellidopaterno = apellidopaterno;
        this.email = email;
        this.id = id;
        this.nombre = nombre;
        this.password = password;
    }
}
