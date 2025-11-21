
Se que eres fisico, pero como yo no tengo casi nada de idea de fisica lo he hecho mirando tutoriales, con posts de reddit y stackoverflow y preguntandole a la ia si tenia sentido.
Por eso el readme es por si me he inventado algo o si he hecho una barbaridad para que por lo menos se entienda el codigo y que queria hacer, funcionar funciona.

Proyecto de Gestión de Escudería F1

1. Generador de Sugerencias (Algoritmo Genético)
He usado un Algoritmo Genético en la clase MejoresCoches, como funciona?
Primero creo un montón de coches totalmente aleatorios usando el método generarConfiguracionAleatoria, la cantidad que creo es la que le paso al metodo

Evaluar si el coche es bueno  
Para saber qué coches merecen la pena, uso el método evaluarConfiguraciones. He puesto una valoracion a cada variable pues un poco como me ha dado la gana pero porque no he encontrado nada concreto

Nota = ((potencia * 0.61) + (downforce * 0.25) + (estado * 0.23) - (peso * 0.37) - (drag * 0.3)) * 10


Ordeno todos los coches según la nota que han sacado antes. Los mejores (la élite) los copio directamente a la siguiente generación para no perderlos. Esto lo hago en el método copiarElite.


Para rellenar el resto de huecos de la nueva generación, cojo a los coches de la élite y les hago cambios pequeños al azar, como cambiarles solo el alerón o los neumáticos, que se cambia cuando le genero e aleatorio a el map con los tipos.
A esto lo llmo variacionesConfiguracion.
La logica es que si un coche ya tiene buena nota, cambiando solo una cosa es probable que encontremos una configuracion con mas nota.
Repito este proceso muchas veces (generaciones). Al final del bucle, el algoritmo me devuelve los coches que han sobrevivido, que matemáticamente son las mejores combinaciones posibles de mi almacén.

2. Simulador de Carreras

-Lo primero que hago es pasar los datos del juego a unidades reales para poder aplicar fórmulas de física:
La Potencia la paso de Caballos a Watts multiplicando por 745.7.
La Masa es la suma de 750kg (chasis) más lo que pesen los componentes. (750 porque lo que mas o menos pesa segun mis calculos como alguien que no tiene ni idea)

-El agarre depende de si llueve o no. Si hay lluvia, el coche usa el valor de agarre en mojado de sus neumáticos.
Uso esta fórmula para la fuerza de rodadura
FuerzaRodadura = Friccion * Masa * 9.81 (Gravedad)

-Cuanto más rápido va el coche, más le frena el aire. He usado la ecuación fundamental de resistencia de fluidos:
ResistenciaAire = 0.5 * 1.225 (DensidadAire) * CoeficienteDrag * AreaFrontal

-Velocidad Máxima Teórica
Aquí aplico la lógica de que la potencia necesaria para vencer la resistencia del aire es la velocidad al cubo. Por tanto, para sacar la velocidad, hago la raíz cúbica:
Velocidad = Raíz Cúbica de (PotenciaEnWatts / ResistenciaTotal)

-Un coche con mucha potencia corre mucho en recta, pero en las curvas necesita fuerza hacia abajo (Downforce) para no salirse.
He programado que la velocidad promedio sea una mezcla: un 60% depende de la velocidad en curva y un 40% de la velocidad en recta.
Si el coche tiene mucho Downforce, le aplico un bonificador a su velocidad en curva usando esta fórmula:
MejoraCurvas = tangenteHiperbolica(Downforce / (Peso * Gravedad))


El Tiempo Final
Sabiendo la velocidad promedio y la longitud del circuito

Tiempo = Distancia / Velocidad

3 Choques, accidentes y fallos mecanicos

Para decidir si un coche se choca, no uso un simple dado. Calculo una variable llamada Lambda que basicamente es la probavilidad de que apse algo para cada vuelta.
Esta tasa sube si:
El circuito es difícil.
Está lloviendo (multiplico por 1.5).
El coche está en mal estado.
El piloto está cansado (aumenta según pasan las vueltas).
Uso una distribución de Poisson para ver si ocurre un evento:
Probabilidad = 1 - e^(-Lambda)

Si ocurre un evento, calculo su gravedad (severidad). Si la gravedad supera un límite (3.5), el coche se retira por accidente grave. Si es menor, solo se le suma tiempo a la vuelta (como una salida de pista).


Aparte de los choques, el coche se puede romper solo. Para esto reviso cada pieza en cada vuelta.
Si la pieza está sana, hay una probabilidad minúscula de fallo electrónico (0.05%).
Si la pieza está muy gastada (menos del 25% de vida), uso una fórmula de curva de fallo (Hazard Rate) que hace que la probabilidad de romperse suba exponencialmente cuanto más usada esté la pieza.


Y hasta ahi todo :)
Se que hay cosas que fallan pero esque no me ha dado tiempo, era o eso o no terminar la logica y dijiste que preferias logica a los cruds;

