package com.salesianostriana.dam.escuderiagonzalodios.models.Enums;

public enum Dificultad {
    FACIL,
    MEDIA,
    DIFICIL,
    MUY_DIFICIL;

    public String getNombreDisplay() {
        return this.name().replaceAll("_", " ");
    }

}
