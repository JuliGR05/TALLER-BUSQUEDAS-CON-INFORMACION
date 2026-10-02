package conmutacion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Simulador {

    /** Separacion en tiempo entre el envio de un paquete y el siguiente. */
    private static final double INTERVALO_ENVIO = 1.0;

    private final Red red;

    /**
     * Estado de cada enlace en el momento de crear el simulador.
     * Los eventos modifican la red para siempre, asi que se guarda una copia
     * para que cada llamada a simular() arranque siempre desde el mismo estado.
     */
    private final List<EstadoEnlace> estadoInicial;

    public Simulador(Red red){
        if (red == null) {
            throw new IllegalArgumentException("La red no puede ser null");
        }
        this.red = red;
        this.estadoInicial = new ArrayList<>();
        for (Enlace e : red.getEnlaces()) {
            estadoInicial.add(new EstadoEnlace(e, e.getCongestion(), e.isActivo()));
        }
    }

    public List<Paquete> simular(List<Mensaje> mensajes, List<Evento> eventos){
        if (mensajes == null) {
            throw new IllegalArgumentException("La lista de mensajes no puede ser null");
        }
        if (eventos == null) {
            eventos = new ArrayList<>();
        }
        // Antes de enviar nada: si un evento apunta a un enlace que no existe,
        // se avisa aqui y no a mitad de la simulacion, con la red ya a medias.
        validarEventos(eventos);

        // Cada corrida parte del estado original de la red.
        restaurarRed();

        List<Paquete> paquetes = new ArrayList<>();
        int indice = 0; // cuenta todos los paquetes enviados, en orden

        for (Mensaje m : mensajes) {
            for (Paquete p : m.getPaquetes()) {
                indice++;

                // 1. Tiempo de envio: el paquete 1 sale en (i-1) * 1.0
                p.setTiempoEnvio((indice - 1) * INTERVALO_ENVIO);

                // 2. Ruta con los pesos actuales de la red
                List<Nodo> ruta = Dijkstra.rutaMasCorta(red, m.getOrigen(), m.getDestino());
                p.getRuta().clear();

                if (ruta.isEmpty()) {
                    // 3.a No hay camino: el paquete no llega
                    p.setTiempoLlegada(Double.POSITIVE_INFINITY);
                } else {
                    // 3.b Llega: tiempo de envio + costo de la ruta
                    p.getRuta().addAll(ruta);
                    p.setTiempoLlegada(p.getTiempoEnvio() + costoRuta(ruta));
                }
                paquetes.add(p);

                // 4. Despues de enviar este paquete, se aplican sus eventos
                for (Evento e : eventos) {
                    if (e.getDespuesDePaquete() == indice) {
                        red.aplicarEvento(e);
                    }
                }
            }
        }

        // 5. Orden de llegada: se ordena por tiempo de llegada (los que no llegan, al final)
        paquetes.sort(Comparator.comparingDouble(Paquete::getTiempoLlegada));
        for (int i = 0; i < paquetes.size(); i++) {
            paquetes.get(i).setOrdenLlegada(i + 1);
        }
        return paquetes;
    }

    // Suma los pesos de los enlaces entre nodos consecutivos de la ruta
    double costoRuta(List<Nodo> ruta) {
        double total = 0;
        for (int i = 0; i < ruta.size() - 1; i++) {
            Enlace e = red.buscarEnlace(ruta.get(i), ruta.get(i + 1));
            total += e.peso();
        }
        return total;
    }

    /** Falla rapido si algun evento apunta a un enlace que no existe en la red. */
    private void validarEventos(List<Evento> eventos) {
        for (Evento e : eventos) {
            Nodo a = red.getNodo(e.getIdNodoA());
            Nodo b = red.getNodo(e.getIdNodoB());
            if (a == null || b == null || red.buscarEnlace(a, b) == null) {
                throw new IllegalArgumentException("El evento apunta a un enlace que no existe: "
                        + e.getIdNodoA() + "-" + e.getIdNodoB());
            }
        }
    }

    /** Devuelve la red al estado que tenia cuando se creo el simulador. */
    private void restaurarRed() {
        for (EstadoEnlace estado : estadoInicial) {
            estado.enlace.setCongestion(estado.congestion);
            estado.enlace.setActivo(estado.activo);
        }
    }

    /** Copia del estado de un enlace, para poder restaurarlo. */
    private static class EstadoEnlace {
        private final Enlace enlace;
        private final double congestion;
        private final boolean activo;

        EstadoEnlace(Enlace enlace, double congestion, boolean activo) {
            this.enlace = enlace;
            this.congestion = congestion;
            this.activo = activo;
        }
    }
}
