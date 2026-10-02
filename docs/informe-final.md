# Taller de búsquedas con información — Informe final

## Conmutación de mensajes con Dijkstra

**Equipo:** Dani-Sunshiny · Juliana Gutiérrez Rodríguez (JuliGR05) ·
Johan Santiago Camargo Olmos (bardock7238) · Mariana Castiblanco Bedoya (mariana0110-hub)

**Reparto real del trabajo:** Punto 1 y la integración quedaron en manos de Dani (#16, #27);
la definición del problema, la función de peso, las clases de mensajes y el análisis de
resultados las trabajó Juliana (#17, #18, #19, #20); el pseudocódigo y este informe los
asumí yo, Johan (#21, #28); y la red con sus escenarios los montó Mariana (#24, #25, #26).
El diseño que nos mantuvo a todos conectados está en el issue #15, que usamos como
contrato: si algo no estaba ahí, se hablaba antes de cambiarlo.

**Código:** paquete `conmutacion` en la rama `develop`.
**Documentos que acompañan este informe:** `docs/punto1-dijkstra-rumania.md` con el punto 1
paso a paso, y `docs/punto2-algoritmo-simulacion.md` con el pseudocódigo del punto 2.
Lo que hago aquí es juntar esas partes, contar lo que salió al correr el programa y dejar
constancia de la revisión que hicimos antes de entregar.

---

## 1. Lo del punto 1, en corto

El punto 1 no tenía código, era lápiz y papel (bueno, tabla y cola de prioridad). Tomamos el
grafo clásico de Rumania, con sus 25 carreteras y sus kilómetros reales, y corrimos Dijkstra
a mano desde Arad hasta Bucharest.

No voy a repetir la tabla entera porque ya está en `punto1-dijkstra-rumania.md`, pero la idea
fue esta: se arranca con Arad en 0 y todo lo demás en infinito, y en cada iteración se saca
el nodo con menor distancia y se relajan sus vecinos. Nos tomó 13 iteraciones llegar a
Bucharest. Lo interesante es que Bucharest apareció primero con 468 km (por Fagaras), después
bajó a 446 (por Craiova) y al final quedó en 418. Si nos hubiéramos quedado con el primer
camino que encontramos, la respuesta habría quedado mal.

La ruta que quedó fue:

**Arad → Sibiu → Rimnicu Vilcea → Pitesti → Bucharest = 140 + 80 + 97 + 101 = 418 km**

contra la alternativa por Fagaras (140 + 99 + 229 = 468 km). 50 km de diferencia.

Lo otro que nos quedó claro del punto 1, y que después nos sirvió para el punto 2, es que
Dijkstra no trabaja con distancia en línea recta sino con el costo real de cada arista.
Sibiu, por ejemplo, está a 187 km de Bucharest en línea recta, pero el camino real más
corto desde ahí cuesta 278 km. Esa diferencia entre "lo que parece cerca" y "lo que cuesta
de verdad" es justo lo que la función de peso trata de capturar en la red de conmutación.

---

## 2. El problema que nos tocó simular

### 2.1 Qué entra y qué sale

La red es un grafo no dirigido. Cada enlace guarda latencia, congestión (0 a 1),
probabilidad de caída (0 a 1), throughput y si está activo o no. Los mensajes se definen
con origen, destino y número de paquetes, y además hay eventos que cambian la red a mitad
de la simulación: algo como "después del paquete 2, el enlace B-R3 se congestiona" o
"se cae el enlace A-R1".

Lo que el programa tiene que devolver, por cada paquete, es por dónde se fue y cuándo
llegó, y al final el orden global de llegada. Eso lo imprime `SalidaConsola`.

Las reglas que acordamos (están en el #15 y las respetamos en el código) son sencillas:
los paquetes salen uno por uno, uno cada 1.0 de tiempo, mensaje por mensaje. Para cada
paquete se calcula la ruta con los pesos de ese momento, y el tiempo de llegada es el
tiempo de envío más lo que cueste la ruta. El evento se aplica justo después del paquete
que dice su número. Si no hay camino, el paquete simplemente no llega (ruta vacía y tiempo
infinito) y queda de último en el orden. Y cada corrida arranca con la red limpia, para que
una simulación no contamine la siguiente.

### 2.2 Cómo se pesa cada enlace

La fórmula está en `CalculadoraPeso.calcular` y hace lo que uno esperaría: si el enlace
está caído, pesa infinito y no se usa. Si no, se suman tres cosas — el retardo
(latencia ajustada por congestión), una penalización por probabilidad de caída y otra por
throughput bajo — y el total se multiplica por 2 si el enlace es de borde.

```text
peso = factorZona * (latencia * (1 + congestión) + 100 * probCaída + 100 / throughput)
factorZona = 2.0 si es de borde, 1.0 si es de núcleo
```

Con los datos de nuestra red eso da, por ejemplo:

| Enlace | Zona | Cuenta | Peso |
|---|---|---|---:|
| R1–R2 (núcleo) | Núcleo | 1.0 × (5.0 + 1.0 + 0.2) | 6.20 |
| A–R1 (borde) | Borde | 2.0 × (10.0 + 5.0 + 1.0) | 32.00 |
| B–R3 con congestión 0.5 | Borde | 2.0 × (15.0 + 5.0 + 1.0) | 42.00 |

Se nota a leguas la intención del diseño: aunque un enlace de borde tenga latencia
parecida a uno de núcleo, termina pesando unas cinco veces más. Y cuando se congestiona,
sube otros 10 puntos, lo suficiente para que el algoritmo prefiera rodear.

### 2.3 El algoritmo (lo esencial)

El pseudocódigo completo quedó en `punto2-algoritmo-simulacion.md`, así que aquí va lo
esencial para no alargarme. El simulador hace esto por cada paquete: le pone su tiempo de
envío, corre Dijkstra con los pesos actuales, le guarda la ruta y le calcula la llegada,
y después aplica los eventos que toquen. Al final ordena todo por llegada.

Lo clave es que Dijkstra se corre **una vez por paquete**, no una vez por mensaje.
Cada ejecución arranca sus vectores de distancia y predecesor desde cero, con una cola
de prioridad, y va relajando vecinos hasta cubrir la red. Como los pesos son no negativos
y el caído vale infinito (se salta), el algoritmo termina siempre. La complejidad por
corrida es la conocida O((V+E) log V); en nuestra red (7 nodos, 11 enlaces) eso no es nada,
pero como se repite por cada paquete, el total anda por O(P·(V+E) log V) más el ordenamiento
final. En la práctica, con nuestros escenarios de 5 a 7 paquetes, es instantáneo.

### 2.4 La red que armamos

La red la construye `Main.construirRed`: cuatro routers de núcleo (R1 a R4) bien
conectados entre sí, y tres usuarios de borde (A, B, C), cada uno colgado de dos routers
distintos para que siempre haya por dónde rodear. Los enlaces de núcleo tienen mejores
números en todo (latencia 4–6 ms, caída 0.01, 500 Mbps) y los de borde peores
(latencia 10–12 ms, caída 0.05, 80–100 Mbps), tal como pedía el enunciado.

| Enlace | Latencia | Caída | Throughput | Peso base |
|---|---:|---:|---:|---:|
| R1–R2 | 5 | 0.01 | 500 | 6.20 |
| R1–R3 | 6 | 0.01 | 500 | 7.20 |
| R2–R3 | 4 | 0.01 | 500 | 5.20 |
| R2–R4 | 6 | 0.01 | 500 | 7.20 |
| R3–R4 | 5 | 0.01 | 500 | 6.20 |
| A–R1 | 10 | 0.05 | 100 | 32.00 |
| A–R2 | 12 | 0.05 | 80 | 36.50 |
| B–R3 | 10 | 0.05 | 100 | 32.00 |
| B–R4 | 12 | 0.05 | 80 | 36.50 |
| C–R2 | 10 | 0.05 | 100 | 32.00 |
| C–R4 | 12 | 0.05 | 80 | 36.50 |

En clases, eso son `ZonaRed`, `Nodo`, `Enlace` y `Red`, con `Main` armando todo y corriendo
los escenarios. No hubo sorpresas acá porque el #15 dejaba las firmas claras
(`buscarEnlace`, `enlacesDe`, `aplicarEvento`, etc.) y cada quien las respetó.

---

## 3. Qué pasó al correr los escenarios

Compilamos con `javac -encoding UTF-8 -d out conmutacion/*.java` y corrimos
`java -cp out conmutacion.Main`. Esto es lo que salió, copiado tal cual de la consola.

**Escenario A: todo estable.** Un mensaje de A a B, 5 paquetes, sin eventos. Todos se
fueron por el mismo lado, como era de esperar.

```text
Orden | Paquete | Llegada | Ruta
  1   | M1-P1   |  71.2   | A -> R1 -> R3 -> B
  2   | M1-P2   |  72.2   | A -> R1 -> R3 -> B
  3   | M1-P3   |  73.2   | A -> R1 -> R3 -> B
  4   | M1-P4   |  74.2   | A -> R1 -> R3 -> B
  5   | M1-P5   |  75.2   | A -> R1 -> R3 -> B
```

La ruta cuesta 71.2 (32.0 + 7.2 + 32.0) y como sale un paquete por unidad de tiempo, las
llegadas van 71.2, 72.2, etc. Sin cambios de peso, no hay nada que recalcular: el orden
de llegada es el mismo de envío.

**Escenario B: congestión en B–R3 después del paquete 2.** Dos mensajes (A→B con 4
paquetes y C→B con 3). El enlace B–R3 pasa de pesar 32 a pesar 42.

```text
Orden | Paquete | Llegada | Ruta
  1   | M1-P1   |  71.2   | A -> R1 -> R3 -> B
  2   | M1-P2   |  72.2   | A -> R1 -> R3 -> B
  3   | M2-P1   |  77.0   | C -> R4 -> B
  4   | M2-P2   |  78.0   | C -> R4 -> B
  5   | M2-P3   |  79.0   | C -> R4 -> B
  6   | M1-P3   |  82.2   | A -> R2 -> R4 -> B
  7   | M1-P4   |  83.2   | A -> R2 -> R4 -> B
```

Acá se ve lo que queríamos probar: los dos primeros paquetes alcanzan a pasar por la ruta
buena, pero del tercero en adelante el simulador ya ve el peso nuevo y manda todo por
`A → R2 → R4 → B` (80.2 de costo). Lo curioso, y lo dejamos así porque es correcto, es que
los paquetes de C, que se enviaron después, llegan antes que la cola de A, porque su ruta
(`C → R4 → B`, 73.0) es más barata. El orden de llegada ya no es el de envío, y eso está bien.

**Escenario C: se cae A–R1 después del paquete 2.** Un mensaje de A a B, 5 paquetes.

```text
Orden | Paquete | Llegada | Ruta
  1   | M1-P1   |  71.2   | A -> R1 -> R3 -> B
  2   | M1-P2   |  72.2   | A -> R1 -> R3 -> B
  3   | M1-P3   |  75.7   | A -> R2 -> R3 -> B
  4   | M1-P4   |  76.7   | A -> R2 -> R3 -> B
  5   | M1-P5   |  77.7   | A -> R2 -> R3 -> B
```

Los dos primeros pasan normal; al caerse A–R1, el resto se va por `A → R2 → R3 → B`
(36.5 + 5.2 + 32.0 = 73.7). Funciona porque el enlace caído pesa infinito y Dijkstra
sencillamente lo ignora.

En resumen: el escenario A confirma que la base está bien calibrada, el B muestra que una
congestión parcial alcanza para desviar tráfico, y el C que una caída total también se
maneja sin romper nada. En los tres, paquetes del mismo mensaje terminan con rutas
distintas cuando hay eventos de por medio, que era justo lo que pedía el taller.

---

## 4. Revisión que hicimos antes de cerrar

Antes de dar esto por terminado crucé el informe con el código, línea por línea en lo
importante: que la fórmula del §2.2 fuera la de `CalculadoraPeso.java`, que los tiempos y
el orden de eventos del §2.3 fueran los de `Simulador.java` (envío `(i-1)*1.0`, evento
después del paquete `i`, orden por llegada), y que las tablas y salidas del §3 fueran las
que bota `Main` de verdad y no números inventados. También revisé ortografía y que los
nombres de clases y métodos coincidieran con el #15 (paquete `conmutacion`, sin tildes).

Queda pendiente lo de siempre en estos trabajos: que cada uno le pegue una leída a la
sección de otro antes de entregar el PDF. Yo ya revisé la parte de red y escenarios contra
el código de Mariana; sería bueno que alguien más revise este informe y el pseudocódigo
contra `Dijkstra.java` y `Simulador.java`.

Con esto los dos puntos del taller quedan cubiertos: el punto 1 con su traza manual,
vectores y ruta de 418 km (§1 y `punto1-dijkstra-rumania.md`), y el punto 2 con problema,
pesos, algoritmo, red y resultados (§2–§3 y `punto2-algoritmo-simulacion.md`). El PDF se
genera desde este mismo archivo.

---

## Referencias

- `docs/punto1-dijkstra-rumania.md` — traza manual Arad → Bucharest (issue #16).
- `docs/punto2-algoritmo-simulacion.md` — pseudocódigo y complejidad (issue #21).
- Issues del repo: #15 (diseño y reglas), #16–#17–#18–#19–#20 (P1–P2), #21–#22–#23–#28 (P3),
  #24–#25–#26–#27 (P4 e integración).
- Código en `conmutacion/`: `ZonaRed`, `Nodo`, `Enlace`, `Red`, `CalculadoraPeso`,
  `Paquete`, `Mensaje`, `Evento`, `Dijkstra`, `Simulador`, `SalidaConsola`, `Main`.
- Cormen et al., *Introduction to Algorithms*, cap. de caminos más cortos (Dijkstra con
  cola de prioridad).
