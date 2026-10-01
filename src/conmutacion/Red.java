package conmutacion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** El grafo no dirigido: nodos (por id) y lista de enlaces. */
public class Red {
    private final Map<String, Nodo> nodos = new LinkedHashMap<>();
    private final List<Enlace> enlaces = new ArrayList<>();

    public void agregarNodo(String id, ZonaRed zona) {
        if (nodos.containsKey(id)) {
            throw new IllegalArgumentException("Ya existe un nodo con id " + id);
        }
        nodos.put(id, new Nodo(id, zona));
    }

    public void conectar(String idA, String idB, double latenciaMs,
                         double probCaida, double throughputMbps) {
        Nodo a = nodos.get(idA);
        Nodo b = nodos.get(idB);
        if (a == null || b == null) {
            throw new IllegalArgumentException("No existe el nodo " + (a == null ? idA : idB));
        }
        if (buscarEnlace(a, b) != null) {
            throw new IllegalArgumentException("Ya existe un enlace entre " + idA + " y " + idB);
        }
        enlaces.add(new Enlace(a, b, latenciaMs, probCaida, throughputMbps));
    }

    public Nodo getNodo(String id) {
        return nodos.get(id);
    }

    public List<Nodo> getNodos() {
        return new ArrayList<>(nodos.values());
    }

    public List<Enlace> getEnlaces() {
        return new ArrayList<>(enlaces);
    }

    /** Enlaces que tocan a ese nodo. */
    public List<Enlace> enlacesDe(Nodo n) {
        List<Enlace> resultado = new ArrayList<>();
        for (Enlace e : enlaces) {
            if (e.getNodoA() == n || e.getNodoB() == n) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    /** El enlace entre a y b, o null si no existe. */
    public Enlace buscarEnlace(Nodo a, Nodo b) {
        for (Enlace e : enlaces) {
            if ((e.getNodoA() == a && e.getNodoB() == b)
                    || (e.getNodoA() == b && e.getNodoB() == a)) {
                return e;
            }
        }
        return null;
    }

    /** Busca el enlace idNodoA-idNodoB y actualiza su congestion y estado. */
    public void aplicarEvento(Evento e) {
        Nodo a = nodos.get(e.getIdNodoA());
        Nodo b = nodos.get(e.getIdNodoB());
        Enlace enlace = (a == null || b == null) ? null : buscarEnlace(a, b);
        if (enlace == null) {
            throw new IllegalArgumentException("El evento apunta a un enlace que no existe: "
                    + e.getIdNodoA() + "-" + e.getIdNodoB());
        }
        enlace.setCongestion(e.getCongestion());
        enlace.setActivo(e.isActivo());
    }
}
