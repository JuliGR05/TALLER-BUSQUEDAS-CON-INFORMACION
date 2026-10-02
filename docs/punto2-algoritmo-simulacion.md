# Punto 2 — Algoritmo de simulación (pseudocódigo)

> Issue #21 — `[P3] Pseudocódigo del algoritmo de simulación`.
> Responsable P3. Clases implicadas: `Simulador`, `Dijkstra`, `Red`, `CalculadoraPeso`.
> Este documento es solo informe: el código ya existe en `conmutacion/Simulador.java` y
> `conmutacion/Dijkstra.java`. El pseudocódigo de abajo describe exactamente lo que hace ese código.

---

## 1. Flujo general

Por cada corrida `Simulador.simular(mensajes, eventos)`:

1. **Dividir mensaje en paquetes**: cada `Mensaje(id, origen, destino, numPaquetes)` ya trae su
   lista `getPaquetes()`. Se recorren mensaje por mensaje, paquete por paquete, en orden de envío.
2. **Calcular ruta**: para cada paquete se corre `Dijkstra.rutaMasCorta(red, origen, destino)`
   con los pesos **actuales** de la red.
3. **Enviar y registrar llegada**: `tiempoEnvio = (i-1) * 1.0` (el paquete global número `i`
   sale en ese instante); `tiempoLlegada = tiempoEnvio + costoRuta` (suma de `peso()` de la ruta).
4. **Aplicar eventos**: justo después de enviar el paquete `i`, se aplican los `Evento`
   con `getDespuesDePaquete() == i` mediante `red.aplicarEvento(e)`.
5. **Ordenar**: al final se ordena por `tiempoLlegada` (los que no llegan, con
   `Double.POSITIVE_INFINITY`, quedan al final) y se asigna `ordenLlegada = 1, 2, 3...`.
6. Si no hay camino, el paquete no llega: ruta vacía y `tiempoLlegada = infinito`.

---

## 2. Pseudocódigo

### 2.1 `simular(mensajes, eventos)`

```text
función simular(mensajes, eventos):
    validarEventos(eventos)          // falla rápido si algún evento apunta a un enlace inexistente
    restaurarRed()                   // cada corrida parte del estado inicial de la red
    paquetes = []
    i = 0                            // contador global de paquetes enviados

    para cada mensaje m en mensajes:
        para cada paquete p en m.getPaquetes():
            i = i + 1
            p.tiempoEnvio = (i - 1) * 1.0

            ruta = Dijkstra.rutaMasCorta(red, m.origen, m.destino)  // con pesos actuales
            p.ruta.clear()
            si ruta está vacía:
                p.tiempoLlegada = INFINITO        // sin camino: no llega
            sino:
                p.ruta.addAll(ruta)
                p.tiempoLlegada = p.tiempoEnvio + costoRuta(ruta)

            paquetes.add(p)

            para cada evento e en eventos:
                si e.despuesDePaquete == i:
                    red.aplicarEvento(e)         // cambia congestión / activo del enlace

    ordenar paquetes por tiempoLlegada ascendente
    para j de 1..paquetes.size():
        paquetes[j].ordenLlegada = j
    retornar paquetes

función costoRuta(ruta):
    total = 0
    para k de 0..ruta.size()-2:
        e = red.buscarEnlace(ruta[k], ruta[k+1])
        total = total + e.peso()
    retornar total
```

Corresponde a `Simulador.simular` (pasos 1–5 comentados en el código) y a
`Simulador.costoRuta`. `INTERVALO_ENVIO = 1.0` está fijo en la clase.

### 2.2 `Dijkstra` (lo que se corre para cada paquete)

```text
función Dijkstra.ejecutar(red, origen):
    para cada nodo n en red: dist[n] = INFINITO; pred[n] = NULL
    dist[origen] = 0
    cola = PriorityQueue con (origen, 0)

    mientras cola no vacía:
        (u, d) = cola.extraerMin()
        si d > dist[u]: continuar              // entrada vieja, ya hay un camino mejor
        para cada enlace e en red.enlacesDe(u):
            peso = e.peso()                    // CalculadoraPeso.calcular(e); caído = INFINITO
            si peso es INFINITO: continuar     // enlace caído: no se relaja
            v = e.otroExtremo(u)
            si dist[u] + peso < dist[v]:
                dist[v] = dist[u] + peso
                pred[v] = u
                cola.insertar(v, dist[v])
    retornar (dist, pred)

función rutaMasCorta(red, origen, destino):
    (dist, pred) = ejecutar(red, origen)
    si dist[destino] es INFINITO: retornar []  // inalcanzable
    ruta = []
    actual = destino
    mientras actual != NULL:
        ruta.add(actual)
        actual = pred[actual]
    invertir(ruta)
    retornar ruta
```

Corresponde a `Dijkstra.ejecutar`, `Dijkstra.Resultado.rutaHasta` y
`Dijkstra.rutaMasCorta`. Los "vectores" del informe son los mapas
`Resultado.dist` (distancias) y `Resultado.pred` (predecesores).

---

## 3. Dónde se recalculan las rutas ante cambios de peso

- **Se recalcula por paquete, no por mensaje.** La llamada a
  `Dijkstra.rutaMasCorta(...)` está dentro del doble bucle mensaje → paquete
  (`Simulador.java`, paso 2), así que cada paquete ve los pesos vigentes en ese momento.
- **Los eventos cambian los pesos entre paquetes.** Después de enviar el paquete `i`
  se ejecuta `red.aplicarEvento(e)`, que hace `enlace.setCongestion(...)` y
  `enlace.setActivo(...)`. Como `Enlace.peso()` delega en
  `CalculadoraPeso.calcular(this)`, el siguiente paquete ya calcula Dijkstra con el
  nuevo peso (mayor congestión → mayor peso; enlace caído → `INFINITO`).
- **Por eso paquetes del mismo mensaje pueden tomar rutas distintas.** Es el comportamiento
  pedido en el diseño (issue #15): escenario B (congestión en `B-R3` tras el paquete 2)
  desvía `M1-P3`/`M1-P4` a `A -> R2 -> R4 -> B`; escenario C (caída de `A-R1` tras el
  paquete 2) desvía a `A -> R2 -> R3 -> B`. Ver salidas en la sección 5.
- **Cada corrida parte del mismo estado.** El constructor guarda `estadoInicial`
  (congestión + activo de cada enlace) y `restaurarRed()` lo repone al inicio de
  `simular()`, así que los eventos de una corrida no contaminan la siguiente.
- **Validación temprana.** `validarEventos(eventos)` comprueba antes de enviar nada que
  `red.getNodo(...)` y `red.buscarEnlace(...)` existen; si no, lanza
  `IllegalArgumentException` en vez de fallar a mitad de la simulación.

---

## 4. Complejidad de Dijkstra con cola de prioridad

Con `V` nodos, `E` enlaces y cola de prioridad binaria (`PriorityQueue` de Java):

- Cada nodo se extrae como mínimo una vez y cada extracción cuesta `O(log V)`.
- Cada arista se relaja como máximo una vez (grafo no dirigido: se evalúa desde cada
  extremo cuando ese extremo sale de la cola) y cada mejora hace un `insert` de
  `O(log V)`.
- Total por ejecución de Dijkstra: **`O((V + E) log V)`** en tiempo,
  `O(V)` en memoria adicional (`dist`, `pred` y la cola).

En la red base del taller (`Main.construirRed`): `V = 7` (R1–R4, A, B, C),
`E = 11` (5 de núcleo + 6 de borde). Con ese tamaño el costo es despreciable; la fórmula
importa porque el simulador corre Dijkstra **una vez por paquete**.

Costo total de `simular()` con `P` paquetes y `Ev` eventos:

| Fase | Costo |
|---|---|
| `P` ejecuciones de Dijkstra | `O(P · (V + E) log V)` |
| `costoRuta` por paquete (recorre la ruta, longitud `L`) | `O(P · L)` |
| Aplicar eventos (se revisan los `Ev` tras cada paquete) | `O(P · Ev)` |
| Orden final por llegada | `O(P log P)` |

Dominante en redes grandes: `O(P · (V + E) log V)`. El `sort` final y el barrido de
eventos son de orden menor. No hay bucles infinitos: Dijkstra termina porque cada
inserción mejora una distancia y los pesos son no negativos (`INFINITO` = enlace caído,
que se salta sin relajar).

---

## 5. Correspondencia pseudocódigo ↔ código

| Pseudocódigo (§2) | Código |
|---|---|
| `simular` pasos 1–5 | `conmutacion/Simulador.java:32-85` |
| `tiempoEnvio = (i-1) * 1.0` | `Simulador.java:54` (`INTERVALO_ENVIO = 1.0`) |
| `Dijkstra.rutaMasCorta` por paquete | `Simulador.java:57` |
| `tiempoLlegada = envio + costoRuta` / `INFINITO` | `Simulador.java:60-67` |
| `aplicarEvento` tras el paquete `i` | `Simulador.java:71-75` → `Red.aplicarEvento` (`Red.java:68-78`) |
| `ordenar por llegada` + `ordenLlegada` | `Simulador.java:80-84` |
| `costoRuta` | `Simulador.java:88-95` (`red.buscarEnlace` + `e.peso()`) |
| `ejecutar` con `PriorityQueue` | `conmutacion/Dijkstra.java:54-107` |
| `dist` / `pred` (vectores) | `Dijkstra.Resultado` (`Dijkstra.java:12-37`) |
| `rutaHasta` / lista vacía si inalcanzable | `Dijkstra.java:18-36` |
| `peso()` / caído = infinito | `Enlace.peso()` (`Enlace.java:58-60`) → `CalculadoraPeso.calcular` |

Verificación ejecutada (`javac -encoding UTF-8 -d out conmutacion/*.java && java -cp out conmutacion.Main`):

- **A (estable, 1 mensaje A→B de 5 paq., sin eventos):** los 5 toman `A -> R1 -> R3 -> B`
  (costo 71.2; llegadas 71.2, 72.2, 73.2, 74.2, 75.2).
- **B (congestión `B-R3 → 0.5` tras paq. 2):** `M1-P1/P2` mantienen la ruta; `M1-P3/P4`
  cambian a `A -> R2 -> R4 -> B` (82.2, 83.2) y `M2-P1..P3` (C→B) usan `C -> R4 -> B`
  (77.0, 78.0, 79.0). El orden de llegada ya no coincide con el de envío.
- **C (caída `A-R1` tras paq. 2):** `M1-P1/P2` por `A -> R1 -> R3 -> B` (71.2, 72.2);
  `M1-P3..P5` por `A -> R2 -> R3 -> B` (75.7, 76.7, 77.7).

Esto demuestra el recálculo por paquete descrito en la sección 3.
