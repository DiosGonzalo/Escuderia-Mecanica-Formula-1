package com.salesianostriana.dam.escuderiagonzalodios.models;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Coche {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String modelo;
    private String imagen;
    private String piloto;
    private double potencia;
    @ManyToMany(mappedBy = "coches")
    private List<Carrera> carreras = new ArrayList<>();

    @OneToMany( mappedBy = "coche", cascade = CascadeType.ALL, orphanRemoval = false)
    @ToString.Exclude
    @Builder.Default
    private List<Componente> componentes = new ArrayList<>();
}
