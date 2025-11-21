package com.salesianostriana.dam.escuderiagonzalodios.models;


import com.salesianostriana.dam.escuderiagonzalodios.models.Enums.Clima;
import com.salesianostriana.dam.escuderiagonzalodios.models.Enums.Dificultad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Carrera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nombre;
    private Date fecha;
    private String imagen;
    @Enumerated(EnumType.STRING)
    private Dificultad dificultad;
    private Long numeroVueltas;
    private double longitudCircuito;
    @Enumerated (EnumType.STRING)
    private Clima clima;
    @ManyToMany (cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(
            name = "carrera_coche",
            joinColumns = @JoinColumn(name = "carreras_id"),
            inverseJoinColumns = @JoinColumn( name = "coche_id")
    )
    private List<Coche> coches = new ArrayList<>();



}
