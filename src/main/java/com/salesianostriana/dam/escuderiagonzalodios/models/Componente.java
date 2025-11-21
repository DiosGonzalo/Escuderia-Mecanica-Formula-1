package com.salesianostriana.dam.escuderiagonzalodios.models;


import com.salesianostriana.dam.escuderiagonzalodios.models.Enums.TipoComponente;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Componente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    @Enumerated(EnumType.STRING)
    private TipoComponente tipo;
    private long limiteUsos;
    private long vecesUsado;
    private double estado;
    private double caballos;
    private double peso;
    private double downforce;
    private double drag;
    private double gripSeco;
    private double gripLluvia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coche_id")
    @ToString.Exclude
    private Coche coche;

}
