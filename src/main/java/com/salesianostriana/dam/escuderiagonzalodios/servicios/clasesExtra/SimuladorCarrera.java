package com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.AccidenteDto;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.CocheDto;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.EstadoCarreraDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class SimuladorCarrera {

    private final Random random = new Random();

    // CONFIGURACIÓN FÁCIL: Ajusta estos números si quieres cambiar la dificultad
    private static final double MULTIPLICADOR_VELOCIDAD_F1 = 3.5; // <--- ESTO ARREGLA EL TIEMPO (De 5h a 1.5h)
    private static final double UMBRAL_ACCIDENTE_GRAVE = 3.5;
    private static final double LAMBDA_SEVERIDAD = 1.5;
    private static final double UMBRAL_FALLO_MECANICO = 25.0;
    private static final double PROBABILIDAD_FALLO_RANDOM = 0.0005;

    // Configuración de Desgaste (Bajo para que aguanten la carrera)
    private static final double DESGASTE_NEUMATICOS = 0.45;
    private static final double DESGASTE_MOTOR_TURBO = 0.18;
    private static final double DESGASTE_GENERICO = 0.15;

    // -------------------------------------------------------------------------

    public static double indiceDificultadNivel(Carrera carrera) {
        return switch (carrera.getDificultad()) {
            case MUY_DIFICIL -> 1;
            case DIFICIL -> 0.75;
            case MEDIA -> 0.5;
            case FACIL -> 0.25;
            default -> 0.5;
        };
    }

    public static double obtenerIntensidadLluvia(Carrera carrera) {
        return switch (carrera.getClima()) {
            case LLUVIA_INTENSA -> 1.0;
            case LLUVIA -> 0.75;
            case NUBLADO -> 0.3;
            case SECO -> 0.0;
            default -> 0.0;
        };
    }

    public static double desviacionCurva(Carrera carrera) {
        double base = 0.08;
        double intensidad = obtenerIntensidadLluvia(carrera);
        return base * (1 + intensidad * 3);
    }

    public double generarNormal(double media, double desviacion) {
        return media + desviacion * random.nextGaussian();
    }

    public double generarExponencial(double lambda) {
        return -Math.log(1.0 - random.nextDouble()) / lambda;
    }

    private static double calcularTasaEventos(Carrera carrera, double estadoPromedio, int vuelta) {
        double lambdaBase = PerformanceCoche.LAMBDA_BASE_EVENTOS;
        double progresoCarrera = (double) vuelta / carrera.getNumeroVueltas();
        double factorDificultad = indiceDificultadNivel(carrera) * 2.0;
        double factorClima = obtenerIntensidadLluvia(carrera) * 1.5;
        double factorEstado = 1.0 + Math.pow(100.0 - estadoPromedio, 2) * 0.0001;
        double factorFatiga = 1.0 + Math.pow(progresoCarrera, 3) * 0.5;

        return lambdaBase * factorDificultad * factorClima * factorEstado * factorFatiga;
    }

    private static double calcularVarianza(double[] valores) {
        if (valores.length == 0) return 0;
        double media = Arrays.stream(valores).average().orElse(0.0);
        double sumaCuadrados = Arrays.stream(valores)
                .map(v -> Math.pow(v - media, 2))
                .sum();
        return sumaCuadrados / valores.length;
    }

    private static double obtenerGripSegunClima(Componente componente, Carrera carrera) {
        double intensidadLluvia = obtenerIntensidadLluvia(carrera);
        double gripSeco = componente.getGripSeco();
        double gripLluvia = componente.getGripLluvia();
        return gripSeco * (1.0 - intensidadLluvia) + gripLluvia * intensidadLluvia;
    }

    public double calcularTiempoVuelta(Coche coche, CocheDto metrics, Carrera carrera, int vuelta, EstadoCarreraDto estado) {

        double gripBase = coche.getComponentes().stream()
                .mapToDouble(c -> obtenerGripSegunClima(c, carrera))
                .average().orElse(0.8);

        double mediaFriccion = PerformanceCoche.COEF_FRICCION_ASFALTO * gripBase;
        double desviacionClima = desviacionCurva(carrera);

        double friccion = generarNormal(mediaFriccion, desviacionClima);
        friccion = Math.max(0.3, Math.min(1.2, friccion));

        double fuerzaRodadura = friccion * metrics.masaTotal * PerformanceCoche.GRAVEDAD;
        double resistenciaDenominador = (0.5 * PerformanceCoche.DENSIDAD_AIRE * metrics.coefDrag * metrics.areaFrontal)
                + (fuerzaRodadura * 0.01);

        double potenciaDegradadaW = metrics.potenciaWatts * Math.exp(PerformanceCoche.FACTOR_DEGRADACION_ESTADO * (100 - metrics.estadoPromedio));
        double velocidadMaxTeorica = Math.pow(potenciaDegradadaW / resistenciaDenominador, 1.0/3.0);

        double indiceDificultad = indiceDificultadNivel(carrera);
        double factorCurvas = Math.exp(PerformanceCoche.COEF_FACTOR_CURVAS * indiceDificultad);

        double velocidadCurvas = velocidadMaxTeorica * factorCurvas * (1 - indiceDificultad * 0.3);
        double velocidadRectas = velocidadMaxTeorica * 0.92;
        double velocidadPromedio = velocidadCurvas * 0.6 + velocidadRectas * 0.4;

        double fuerzaDownforce = 0.5 * PerformanceCoche.DENSIDAD_AIRE * metrics.downforceTotal * Math.pow(velocidadPromedio, 2);
        double mejoraCurvas = Math.tanh(fuerzaDownforce / (metrics.masaTotal * PerformanceCoche.GRAVEDAD * 10)) * 0.15;
        velocidadPromedio *= (1 + mejoraCurvas);

        double progresoCarrera = (double) vuelta / carrera.getNumeroVueltas();
        double factorFatiga = Math.exp(-Math.pow(progresoCarrera * 2, 1.5) * 0.15);
        velocidadPromedio *= factorFatiga;

        double vueltasCalentamiento = Math.min(vuelta, 3);
        double tempOptima = Math.exp(-Math.pow((vueltasCalentamiento - 2), 2) / 2);
        velocidadPromedio *= (0.92 + tempOptima * 0.08);

        // --- AQUÍ ESTÁ EL ARREGLO ---
        // 1. Pasamos km a metros (* 1000)
        // 2. Multiplicamos la velocidad por el factor F1 (3.5) para corregir la física
        double tiempoBase = (carrera.getLongitudCircuito() * 1000) / (velocidadPromedio * MULTIPLICADOR_VELOCIDAD_F1);

        double variabilidad = Math.exp(generarNormal(0, 0.02));
        tiempoBase *= variabilidad;

        double lambda = calcularTasaEventos(carrera, metrics.estadoPromedio, vuelta);
        double probEvento = 1.0 - Math.exp(-lambda);

        if (random.nextDouble() < probEvento) {
            double severidad = generarExponencial(LAMBDA_SEVERIDAD);
            tiempoBase *= (1.0 + severidad);

            if (severidad > UMBRAL_ACCIDENTE_GRAVE) {
                estado.setRetirado(true);
                estado.setVueltaRetiro(vuelta);
                estado.setMotivoRetiro("Accidente grave (Salida de vía)");
            }
        }

        double[] estadosComponentes = coche.getComponentes().stream().mapToDouble(Componente::getEstado).toArray();
        double varianzaEstados = calcularVarianza(estadosComponentes);
        double penalizacionDesbalance = 1.0 + (varianzaEstados / PerformanceCoche.PENALIZACION_DESBALANCE);

        return tiempoBase * penalizacionDesbalance;
    }

    public void aplicarDesgasteFisico(Coche coche, Carrera carrera) {
        double factorDificultad = indiceDificultadNivel(carrera) * 0.6;
        double intensidadLluvia = obtenerIntensidadLluvia(carrera);
        double factorClima = 1.0 + intensidadLluvia * 0.5;

        for (Componente c : coche.getComponentes()) {
            double tasaComponenteBase = obtenerFactorDesgasteComponente(c);
            double stresTermico = Math.pow((double) c.getVecesUsado() / c.getLimiteUsos(), 1.5);

            double desgasteBruto = tasaComponenteBase * factorDificultad * factorClima * (1.0 + stresTermico * 0.5);
            double factorRuido = 1.0 + generarNormal(0, 0.15);

            double desgasteFinal = desgasteBruto * factorRuido;

            c.setVecesUsado(c.getVecesUsado() + 1);
            double nuevoEstado = c.getEstado() - desgasteFinal;
            c.setEstado(Math.max(0, nuevoEstado));
        }
    }

    public double obtenerFactorDesgasteComponente(Componente c) {
        return switch (c.getTipo()) {
            case MOTOR, TURBO -> DESGASTE_MOTOR_TURBO;
            case NEUMATICOS -> DESGASTE_NEUMATICOS;
            default -> DESGASTE_GENERICO;
        };
    }

    public void verificarFallos(Coche coche, EstadoCarreraDto estado, int vuelta, List<AccidenteDto> eventos) {
        for (Componente c : coche.getComponentes()) {

            // Si el componente está sano (>25%), no falla
            if (c.getEstado() > UMBRAL_FALLO_MECANICO) {
                // Pequeña probabilidad de fallo electrónico random
                if (random.nextDouble() < PROBABILIDAD_FALLO_RANDOM) {
                    provocarFallo(coche, estado, vuelta, eventos, c, "Fallo electrónico inesperado");
                    return;
                }
                continue;
            }

            double shape = 2.5;
            double scale = c.getLimiteUsos() * 1.2;
            double tiempoUso = c.getVecesUsado();

            double hazardRate = (shape / scale) * Math.pow(tiempoUso / scale, shape - 1);
            double probFallo = 1.0 - Math.exp(-hazardRate);

            probFallo *= Math.exp((UMBRAL_FALLO_MECANICO - c.getEstado()) / 5.0);

            if (random.nextDouble() < probFallo) {
                String motivo = String.format("Fallo mecánico: %s (Estado: %.1f%%)", c.getNombre(), c.getEstado());
                provocarFallo(coche, estado, vuelta, eventos, c, motivo);
                return;
            }
        }
    }

    private void provocarFallo(Coche coche, EstadoCarreraDto estado, int vuelta, List<AccidenteDto> eventos, Componente c, String motivo) {
        estado.setRetirado(true);
        estado.setVueltaRetiro(vuelta);
        estado.setMotivoRetiro(motivo);
        eventos.add(new AccidenteDto("FALLO_MECANICO", coche, vuelta, 1.0, motivo));
    }
}