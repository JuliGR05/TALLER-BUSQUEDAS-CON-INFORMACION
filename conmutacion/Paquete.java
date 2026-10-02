package conmutacion;

import java.util.ArrayList;
import java.util.List;

/** Un paquete de un mensaje: lleva su numero, la ruta que tomo y sus tiempos. */
public class Paquete {
    private final int idMensaje;
    private final int numero;
    private final List<Nodo> ruta = new ArrayList<>();
    private double tiempoEnvio;
    private double tiempoLlegada;
    private int ordenLlegada;

    public Paquete(int idMensaje, int numero) {
        this.idMensaje = idMensaje;
        this.numero = numero;
    }

    public int getIdMensaje() { return idMensaje; }
    public int getNumero() { return numero; }
    public List<Nodo> getRuta() { return ruta; }
    public double getTiempoEnvio() { return tiempoEnvio; }
    public void setTiempoEnvio(double tiempoEnvio) { this.tiempoEnvio = tiempoEnvio; }
    public double getTiempoLlegada() { return tiempoLlegada; }
    public void setTiempoLlegada(double tiempoLlegada) { this.tiempoLlegada = tiempoLlegada; }
    public int getOrdenLlegada() { return ordenLlegada; }
    public void setOrdenLlegada(int ordenLlegada) { this.ordenLlegada = ordenLlegada; }
}
