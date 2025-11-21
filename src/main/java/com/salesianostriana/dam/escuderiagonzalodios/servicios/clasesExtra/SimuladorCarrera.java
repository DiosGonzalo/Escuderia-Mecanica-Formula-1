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

    private static final double multiplicadorVelocidadF1 = 3.5;
    private static final double umbralAccidenteGrave = 3.5;
    private static final double lambdaSeveridad = 1.5;
    private static final double umbralFalloMecanico = 25.0;
    private static final double probabilidadFalloRandom = 0.0005;

    private static final double desgasteNeumaticos = 0.45;
    private static final double desgasteMotorTurbo = 0.18;
    private static final double desgasteGenerico = 0.15;

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
        double base;
        double intensidad;

        base = 0.08;
        intensidad = obtenerIntensidadLluvia(carrera);
        return base * (1 + intensidad * 3);
    }

    public double generarNormal(double media, double desviacion) {
        return media + desviacion * random.nextGaussian();
    }

    public double generarExponencial(double lambda) {
        return -Math.log(1.0 - random.nextDouble()) / lambda;
    }

    private static double calcularTasaEventos(Carrera carrera, double estadoPromedio, int vuelta) {
        double lambdaBase;
        double progresoCarrera;
        double factorDificultad;
        double factorClima;
        double factorEstado;
        double factorFatiga;

        lambdaBase = PerformanceCoche.lambdaEventos;
        progresoCarrera = (double) vuelta / carrera.getNumeroVueltas();
        factorDificultad = indiceDificultadNivel(carrera) * 2.0;
        factorClima = obtenerIntensidadLluvia(carrera) * 1.5;
        factorEstado = 1.0 + Math.pow(100.0 - estadoPromedio, 2) * 0.0001;
        factorFatiga = 1.0 + Math.pow(progresoCarrera, 3) * 0.5;

        return lambdaBase * factorDificultad * factorClima * factorEstado * factorFatiga;
    }

    private static double calcularVarianza(double[] valores) {
        if (valores.length == 0) return 0;
        double media;
        double sumaCuadrados;

        media = Arrays.stream(valores).average().orElse(0.0);
        sumaCuadrados = Arrays.stream(valores)
                .map(v -> Math.pow(v - media, 2))
                .sum();
        return sumaCuadrados / valores.length;
    }

    private static double obtenerGripSegunClima(Componente componente, Carrera carrera) {
        double intensidadLluvia;
        double gripSeco;
        double gripLluvia;

        intensidadLluvia = obtenerIntensidadLluvia(carrera);
        gripSeco = componente.getGripSeco();
        gripLluvia = componente.getGripLluvia();
        return gripSeco * (1.0 - intensidadLluvia) + gripLluvia * intensidadLluvia;
    }

    public double calcularTiempoVuelta(Coche coche, CocheDto metrics, Carrera carrera, int vuelta, EstadoCarreraDto estado) {
        double gripBase, mediaFriccion, desviacionClima, friccion;
        double fuerzaRodadura, resistenciaDenominador;
        double potenciaDegradadaW, velocidadMaxTeorica;
        double indiceDificultad, factorCurvas;
        double velocidadCurvas, velocidadRectas, velocidadPromedio;
        double fuerzaDownforce, mejoraCurvas;
        double progresoCarrera, factorFatiga;
        double vueltasCalentamiento, tempOptima;
        double tiempoBase, variabilidad;
        double lambda, probEvento, severidad;
        double[] estadosComponentes;
        double varianzaEstados, penalizacionDesbalance;

        gripBase = coche.getComponentes().stream()
                .mapToDouble(c -> obtenerGripSegunClima(c, carrera))
                .average().orElse(0.8);

        mediaFriccion = PerformanceCoche.cofFriccion * gripBase;
        desviacionClima = desviacionCurva(carrera);

        friccion = generarNormal(mediaFriccion, desviacionClima);
        friccion = Math.max(0.3, Math.min(1.2, friccion));

        fuerzaRodadura = friccion * metrics.masaTotal * PerformanceCoche.gravedad;
        resistenciaDenominador = (0.5 * PerformanceCoche.densidadAire * metrics.coefDrag * metrics.areaFrontal)
                + (fuerzaRodadura * 0.01);

        potenciaDegradadaW = metrics.potenciaWatts * Math.exp(PerformanceCoche.factorDegradacion * (100 - metrics.estadoPromedio));
        velocidadMaxTeorica = Math.pow(potenciaDegradadaW / resistenciaDenominador, 1.0/3.0);

        indiceDificultad = indiceDificultadNivel(carrera);
        factorCurvas = Math.exp(PerformanceCoche.coefFactorCurvas * indiceDificultad);

        velocidadCurvas = velocidadMaxTeorica * factorCurvas * (1 - indiceDificultad * 0.3);
        velocidadRectas = velocidadMaxTeorica * 0.92;
        velocidadPromedio = velocidadCurvas * 0.6 + velocidadRectas * 0.4;

        fuerzaDownforce = 0.5 * PerformanceCoche.densidadAire * metrics.downforceTotal * Math.pow(velocidadPromedio, 2);
        mejoraCurvas = Math.tanh(fuerzaDownforce / (metrics.masaTotal * PerformanceCoche.gravedad * 10)) * 0.15;
        velocidadPromedio *= (1 + mejoraCurvas);

        progresoCarrera = (double) vuelta / carrera.getNumeroVueltas();
        factorFatiga = Math.exp(-Math.pow(progresoCarrera * 2, 1.5) * 0.15);
        velocidadPromedio *= factorFatiga;

        vueltasCalentamiento = Math.min(vuelta, 3);
        tempOptima = Math.exp(-Math.pow((vueltasCalentamiento - 2), 2) / 2);
        velocidadPromedio *= (0.92 + tempOptima * 0.08);

        tiempoBase = (carrera.getLongitudCircuito() * 1000) / (velocidadPromedio * multiplicadorVelocidadF1);

        variabilidad = Math.exp(generarNormal(0, 0.02));
        tiempoBase *= variabilidad;

        lambda = calcularTasaEventos(carrera, metrics.estadoPromedio, vuelta);
        probEvento = 1.0 - Math.exp(-lambda);

        if (random.nextDouble() < probEvento) {
            severidad = generarExponencial(lambdaSeveridad);
            tiempoBase *= (1.0 + severidad);

            if (severidad > umbralAccidenteGrave) {
                estado.setRetirado(true);
                estado.setVueltaRetiro(vuelta);
                estado.setMotivoRetiro("Accidente grave (Salida de vía)");
            }
        }

        estadosComponentes = coche.getComponentes().stream().mapToDouble(Componente::getEstado).toArray();
        varianzaEstados = calcularVarianza(estadosComponentes);
        penalizacionDesbalance = 1.0 + (varianzaEstados / PerformanceCoche.penalizacionBalance);

        return tiempoBase * penalizacionDesbalance;
    }

    public void aplicarDesgasteFisico(Coche coche, Carrera carrera) {
        double factorDificultad;
        double intensidadLluvia;
        double factorClima;
        double tasaComponenteBase;
        double stresTermico;
        double desgasteBruto;
        double factorRuido;
        double desgasteFinal;
        double nuevoEstado;

        factorDificultad = indiceDificultadNivel(carrera) * 0.6;
        intensidadLluvia = obtenerIntensidadLluvia(carrera);
        factorClima = 1.0 + intensidadLluvia * 0.5;

        for (Componente c : coche.getComponentes()) {
            tasaComponenteBase = obtenerFactorDesgasteComponente(c);
            stresTermico = Math.pow((double) c.getVecesUsado() / c.getLimiteUsos(), 1.5);

            desgasteBruto = tasaComponenteBase * factorDificultad * factorClima * (1.0 + stresTermico * 0.5);
            factorRuido = 1.0 + generarNormal(0, 0.15);

            desgasteFinal = desgasteBruto * factorRuido;

            c.setVecesUsado(c.getVecesUsado() + 1);
            nuevoEstado = c.getEstado() - desgasteFinal;
            c.setEstado(Math.max(0, nuevoEstado));
        }
    }

    public double obtenerFactorDesgasteComponente(Componente c) {
        return switch (c.getTipo()) {
            case MOTOR, TURBO -> desgasteMotorTurbo;
            case NEUMATICOS -> desgasteNeumaticos;
            default -> desgasteGenerico;
        };
    }

    public void verificarFallos(Coche coche, EstadoCarreraDto estado, int vuelta, List<AccidenteDto> eventos) {
        double shape;
        double scale;
        double tiempoUso;
        double hazardRate;
        double probFallo;
        String motivo;

        for (Componente c : coche.getComponentes()) {
            if (c.getEstado() > umbralFalloMecanico) {
                if (random.nextDouble() < probabilidadFalloRandom) {
                    provocarFallo(coche, estado, vuelta, eventos, c, "Fallo electrónico inesperado");
                    return;
                }
                continue;
            }

            shape = 2.5;
            scale = c.getLimiteUsos() * 1.2;
            tiempoUso = c.getVecesUsado();

            hazardRate = (shape / scale) * Math.pow(tiempoUso / scale, shape - 1);
            probFallo = 1.0 - Math.exp(-hazardRate);

            probFallo *= Math.exp((umbralFalloMecanico - c.getEstado()) / 5.0);

            if (random.nextDouble() < probFallo) {
                motivo = String.format("Fallo mecánico: %s (Estado: %.1f%%)", c.getNombre(), c.getEstado() * 100);
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