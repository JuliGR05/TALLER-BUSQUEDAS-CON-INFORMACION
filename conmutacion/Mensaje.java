package conmutacion;

import java.util.ArrayList;
import java.util.List;

public class Mensaje {
    private final int id;
    private final Nodo origen;
    private final Nodo destino;
    private final List<Paquete> paquetes = new ArrayList<>();

    public Mensaje(int id, Nodo origen, Nodo destino, int numPaquetes){
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
