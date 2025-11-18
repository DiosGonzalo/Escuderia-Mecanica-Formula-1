package com.salesianostriana.dam.escuderiagonzalodios.models.Enums;

public enum Dificultad {
    FACIL,
    MEDIA,
    DIFICIL;

    public String getNombreDisplay() {
        return this.name().replaceAll("_", " ");
    }

}
