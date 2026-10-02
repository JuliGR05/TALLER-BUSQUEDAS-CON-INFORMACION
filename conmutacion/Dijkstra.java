package conmutacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Dijkstra{
    //Guarda resultado de correr Dijkstra desde cierto origen
     public static class Resultado {
        public final Map<Nodo, Double> dist = new HashMap<>();  // vector de distancias
        public final Map<Nodo, Nodo> pred = new HashMap<>();    // vector de predecesores
    
        // Reconstruye el camino origen -> destino siguiendo los predecesores
        public List<Nodo> rutaHasta(Nodo destino){
            List<Nodo> ruta = new ArrayList<>();

        // Si no hay distancia o es infinita, el destino es inalcanzable
        Double d = dist.get(destino);
        if(d == null || Double.isInfinite(d)){
            return ruta; // lista vacía
        }

        // Se camina hacia atrás: destino, predecesor de este y así...
        Nodo actual = destino;
        while (actual != null){
            ruta.add(actual);
            actual = pred.get(actual);
        }

        // Quedó al revés (destino -> origen), se invierte
        Collections.reverse(ruta);
        return ruta;
        }
    }        
// Pareja (nodo, distancia) que se guarda en la cola de prioridad
    private static class Entrada implements Comparable<Entrada> {
        final Nodo nodo;
        final double distancia;

        Entrada(Nodo nodo, double distancia) {
            this.nodo = nodo;
            this.distancia = distancia;
        }

        @Override
        public int compareTo(Entrada otra) {
            return Double.compare(this.distancia, otra.distancia);
        }
    }

    public static Resultado ejecutar(Red red, Nodo origen) {
        // Validar aqui evita que un origen ajeno a la red devuelva una ruta
        // vacia en silencio, haciendolo pasar por "destino inalcanzable".
        if (red == null) {
            throw new IllegalArgumentException("La red no puede ser null");
        }
        if (origen == null) {
            throw new IllegalArgumentException("El origen no puede ser null");
        }
        if (red.getNodo(origen.getId()) != origen) {
            throw new IllegalArgumentException("El nodo origen " + origen + " no pertenece a la red");
        }

        Resultado r = new Resultado();

        // 1. Inicialización: todo infinito y sin predecesor, menos el origen
        for (Nodo n : red.getNodos()) {
            r.dist.put(n, Double.POSITIVE_INFINITY);
            r.pred.put(n, null);
        }
        r.dist.put(origen, 0.0);

        // 2. La cola siempre entrega primero el nodo con menor distancia
        PriorityQueue<Entrada> cola = new PriorityQueue<>();
        cola.add(new Entrada(origen, 0.0));

        while (!cola.isEmpty()) {
            Entrada actual = cola.poll();
            Nodo u = actual.nodo;

            // Entrada vieja: ya se encontró un camino mejor a u, se ignora
            if (actual.distancia > r.dist.get(u)) {
                continue;
            }

            // 3. Relajación de los enlaces que salen de u
            for (Enlace e : red.enlacesDe(u)) {
                double peso = e.peso();
                if (Double.isInfinite(peso)) {
                    continue; // enlace caído: no se usa
                }

                Nodo v = e.otroExtremo(u);
                double nueva = r.dist.get(u) + peso;

                if (nueva < r.dist.get(v)) {
                    r.dist.put(v, nueva);
                    r.pred.put(v, u);
                    cola.add(new Entrada(v, nueva));
                }
            }
        }
        return r;
    }

    public static List<Nodo> rutaMasCorta(Red red, Nodo origen, Nodo destino) {
        if (destino == null) {
            throw new IllegalArgumentException("El destino no puede ser null");
        }
        return ejecutar(red, origen).rutaHasta(destino);
    }
}
