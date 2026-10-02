package conmutacion;

import java.util.ArrayList;
import java.util.List;

/** Un mensaje que se divide en numPaquetes paquetes, todos con la misma ruta origen -> destino. */
public class Mensaje {
    private final int id;
    private final Nodo origen;
    private final Nodo destino;
    private final List<Paquete> paquetes = new ArrayList<>();

    public Mensaje(int id, Nodo origen, Nodo destino, int numPaquetes){
        // Sin estas validaciones un mensaje con origen null o sin paquetes
        // se aceptaba aqui y fallaba (o desaparecia) durante la simulación.
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("El origen y el destino del mensaje no pueden ser null");
        }
        if (numPaquetes < 1) {
            throw new IllegalArgumentException("Un mensaje debe tener al menos 1 paquete");
        }
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        for (int i = 1; i <= numPaquetes; i++){
            paquetes.add(new Paquete(id, i));
        }
    }

    public int getId() {return id;}
    public Nodo getOrigen(){return origen;}
    public Nodo getDestino() {return destino;}
    public List<Paquete> getPaquetes(){return paquetes;}
}
