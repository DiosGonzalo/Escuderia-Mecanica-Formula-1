package com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.AccidenteDto;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.CocheDto;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.EstadoCarreraDto;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimuladorCarrera {

    private final Random random = new Random();

    // ----------------------------------------------------
    // METODOS AUXILIARES DE CARRERA (Static)
    // ----------------------------------------------------

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

    // ----------------------------------------------------
    // METODOS DE ESTADÍSTICA (Instancia)
    // ----------------------------------------------------

    public double generarNormal(double media, double desviacion) {
        return media + desviacion * random.nextGaussian();
    }

    // Renombrado de 'exponencial' a 'generarExponencial' por coherencia
    public double generarExponencial(double lambda) {
        return -Math.log(1.0 - random.nextDouble()) / lambda;
    }

    private static double calcularTasaEventos(Carrera carrera, double estadoPromedio, int vuelta) {
        double lambdaBase = PerformanceCoche.LAMBDA_BASE_EVENTOS;
        double progresoCarrera = (double) vuelta / carrera.getNumeroVueltas();

        double factorDificultad = indiceDificultadNivel(carrera) * 2.0;
        double factorClima = obtenerIntensidadLluvia(carrera) * 1.5;

        // Riesgo aumenta si el estado es bajo
        double factorEstado = 1.0 + Math.pow(100.0 - estadoPromedio, 2) * 0.0001;

        // Riesgo aumenta al final de la carrera
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
    // METODO DE SIMULACIÓN DE VUELTA (El Método Principal)

    public double calcularTiempoVuelta(Coche coche, CocheDto metrics, Carrera carrera, int vuelta, EstadoCarreraDto estado) {

        // --- 1. CÁLCULO DE GRIP Y FRICCIÓN (Rodadura) ---
        double gripBase = coche.getComponentes().stream()
                .mapToDouble(c -> obtenerGripSegunClima(c, carrera))
                .average().orElse(0.8);

        double mediaFriccion = PerformanceCoche.COEF_FRICCION_ASFALTO * gripBase;
        double desviacionClima = desviacionCurva(carrera);

        double friccion = generarNormal(mediaFriccion, desviacionClima);
        friccion = Math.max(0.3, Math.min(1.2, friccion));

        // --- 2. VELOCIDAD MÁXIMA TEÓRICA (Vmax) ---
        double fuerzaRodadura = friccion * metrics.masaTotal * PerformanceCoche.GRAVEDAD;

        double resistenciaDenominador = (0.5 * PerformanceCoche.DENSIDAD_AIRE * metrics.coefDrag * metrics.areaFrontal)
                + (fuerzaRodadura * 0.01);

        double potenciaDegradadaW = metrics.potenciaWatts * Math.exp(PerformanceCoche.FACTOR_DEGRADACION_ESTADO * (100 - metrics.estadoPromedio));

        double velocidadMaxTeorica = Math.pow(potenciaDegradadaW / resistenciaDenominador, 1.0/3.0);


        // --- 3. VELOCIDAD PROMEDIO (Vpromedio) ---

        // a) Ajuste por Circuito y Downforce
        double indiceDificultad = indiceDificultadNivel(carrera);
        double factorCurvas = Math.exp(PerformanceCoche.COEF_FACTOR_CURVAS * indiceDificultad);

        double velocidadCurvas = velocidadMaxTeorica * factorCurvas * (1 - indiceDificultad * 0.3);
        double velocidadRectas = velocidadMaxTeorica * 0.92;
        double velocidadPromedio = velocidadCurvas * 0.6 + velocidadRectas * 0.4;

        // CORRECCIÓN DEL ERROR AQUÍ: Usamos PerformanceCoche.DENSIDAD_AIRE y metrics.downforce
        double fuerzaDownforce = 0.5 * PerformanceCoche.DENSIDAD_AIRE * metrics.downforceTotal * Math.pow(velocidadPromedio, 2);
        double mejoraCurvas = Math.tanh(fuerzaDownforce / (metrics.masaTotal * PerformanceCoche.GRAVEDAD * 10)) * 0.15;
        velocidadPromedio *= (1 + mejoraCurvas);

        // b) Ajuste por Factores Dinámicos (Fatiga y Neumáticos)
        double progresoCarrera = (double) vuelta / carrera.getNumeroVueltas();
        double factorFatiga = Math.exp(-Math.pow(progresoCarrera * 2, 1.5) * 0.15);
        velocidadPromedio *= factorFatiga;

        double vueltasCalentamiento = Math.min(vuelta, 3);
        double tempOptima = Math.exp(-Math.pow((vueltasCalentamiento - 2), 2) / 2);
        velocidadPromedio *= (0.92 + tempOptima * 0.08);


        // --- 4. TIEMPO BASE Y AJUSTES ESTOCÁSTICOS ---
        double tiempoBase = carrera.getLongitudCircuito() / velocidadPromedio;

        // Variabilidad Estocástica
        double variabilidad = Math.exp(generarNormal(0, 0.02));
        tiempoBase *= variabilidad;

        // Eventos Probabilísticos (Accidentes)
        double lambda = calcularTasaEventos(carrera, metrics.estadoPromedio, vuelta);
        double probEvento = 1.0 - Math.exp(-lambda);

        if (random.nextDouble() < probEvento) {
            double severidad = generarExponencial(1.5);
            tiempoBase *= (1.0 + severidad);

            if (severidad > 2.5) {
                estado.setRetirado(true);
                estado.setVueltaRetiro(vuelta);
                estado.setMotivoRetiro("Accidente catastrófico durante la vuelta.");
            }
        }

        // Penalización por Desbalance
        double[] estadosComponentes = coche.getComponentes().stream().mapToDouble(Componente::getEstado).toArray();
        double varianzaEstados = calcularVarianza(estadosComponentes);

        double penalizacionDesbalance = 1.0 + (varianzaEstados / PerformanceCoche.PENALIZACION_DESBALANCE);

        return tiempoBase * penalizacionDesbalance;
    }

    // ----------------------------------------------------
    // METODOS DE GESTIÓN DE ESTADO (Desgaste y Fallos)
    // ----------------------------------------------------

    public void aplicarDesgasteFisico(Coche coche, Carrera carrera) {
        // ... (Tu implementación de desgaste aquí, usa generarNormal) ...
        double factorDificultad = indiceDificultadNivel(carrera) * 0.6;
        double intensidadLluvia = obtenerIntensidadLluvia(carrera);
        double factorClima = 1.0 + intensidadLluvia * 0.5;

        for (Componente c : coche.getComponentes()) {
            double tasaComponenteBase = obtenerFactorDesgasteComponente(c);
            double stresTermico = Math.pow((double) c.getVecesUsado() / c.getLimiteUsos(), 1.5);

            double desgasteBruto = tasaComponenteBase * factorDificultad * factorClima * (1.0 + stresTermico * 0.5);
            double factorRuido = 1.0 + generarNormal(0, 0.15); // Usa el método de instancia generarNormal()

            double desgasteFinal = desgasteBruto * factorRuido;

            c.setVecesUsado(c.getVecesUsado() + 1);
            double nuevoEstado = c.getEstado() - desgasteFinal;
            c.setEstado(Math.max(0, nuevoEstado));
        }
    }

    public double obtenerFactorDesgasteComponente(Componente c) {
        return switch (c.getTipo()) {
            case MOTOR, TURBO -> 1.8;
            case NEUMATICOS -> 3.5;
            case DIRECCION -> 2.5;
            case SUSPENSION -> 1.2;
            case PARAGOLPES -> 1.9;
            case ALERON -> 2.1;
            case BATERIA -> 1.1;
            case CAJA_DE_CAMBIOS -> 1.6;
            default -> 1.0;
        };
    }

    public void verificarFallos(Coche coche, EstadoCarreraDto estado, int vuelta, List<AccidenteDto> eventos) {
        // ... (Tu implementación de verificación de fallos aquí) ...
        for (Componente c : coche.getComponentes()) {
            double shape = 2.5;
            double scale = c.getLimiteUsos() * 0.9;
            double tiempoUso = c.getVecesUsado();

            double hazardRate = (shape / scale) * Math.pow(tiempoUso / scale, shape - 1);
            double probFallo = 1.0 - Math.exp(-hazardRate);

            if (c.getEstado() < 15) {
                probFallo *= Math.exp((15.0 - c.getEstado()) / 8.0);
            }

            if (random.nextDouble() < probFallo) {
                estado.setRetirado(true);
                estado.setVueltaRetiro(vuelta);

                String motivo = String.format("Fallo catastrófico: %s (Estado: %.1f%%)",
                        c.getNombre(), c.getEstado());
                estado.setMotivoRetiro(motivo);

                eventos.add(new AccidenteDto("FALLO_FATIGA", coche, vuelta, probFallo, motivo));
                return;
            }
        }
    }
}