package conmutacion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Simulador {
    private final Red red;

    public Simulador(Red red){
        this.red = red;
    }

    public List<Paquete> simular(List<Mensaje> mensajes, List<Evento> eventos){
        if(eventos == null){
            eventos = new ArrayList<>();
        }

        List<Paquete> paquetes = new ArrayList<>();
        int indice = 0; //cuenta todos los paquetes enviados, en orden

        for(Mensaje m : mensajes){
            for(Paquete p : m.getPaquetes()){
                indice++;

                //1. Tiempo de nvío: el paquete 1 sale en (i-1) * 1.0
                p.setTiempoEnvio((indice - 1) * 1.0);

                //2. Ruta con los pesos actuales de lared
                List<Nodo> ruta = Dijkstra.rutaMasCorta(red, m.getOrigen(), m.getDestino());
                p.getRuta().clear();

                if(ruta.isEmpty()){
                    //3.a NO hay camino: el paquete no llega
                    p.setTiempoLlegada(Double.POSITIVE_INFINITY);
                } else {
                    //3b. Llega: Tiempo de envío + costo de la ruta
                    p.getRuta().addAll(ruta);
                    p.setTiempoLlegada(p.getTiempoEnvio() + costoRuta(ruta));
                }
                paquetes.add(p);

                //4. Después de enviar este paquete, se aplican sus eventos
                for(Evento e : eventos){
                    if(e.getDespuesDePaquete() == indice){
                        red.aplicarEvento(e);
                    }
                }
            }
        }

                //5. Orden de llegada: se ordena por tiempo de llegada (los que no llegan,al final)
                paquetes.sort(Comparator.comparingDouble(Paquete::getTiempoLlegada));
                for (int i = 0; i<paquetes.size(); i++){
                    paquetes.get(i).setOrdenLlegada(i + 1);
            }
                return paquetes;
        }

                //Suma los pesos de los enlaces entre nodos consecutivos de la ruta
                double costoRuta(List<Nodo> ruta){
                    double total = 0;
                    for (int i = 0; i < ruta.size() - 1; i++){
                        Enlace e = red.buscarEnlace(ruta.get(i), ruta.get(i+1));
                        total += e.peso();
                    }
                    return total;
                }
}
