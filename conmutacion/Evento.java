package conmutacion;

/**
 * Cambio en un enlace durante la simulación.
 * Significa: "después de enviar el paquete numero despuesDePaquete,
 * el enlace idNodoA-idNodoB pasa a tener esta congestión y este estado".
 */
public class Evento {
    private final int despuesDePaquete;
    private final String idNodoA;
    private final String idNodoB;
    private final double congestion;
    private final boolean activo;

    public Evento(int despuesDePaquete, String idNodoA, String idNodoB,
                  double congestion, boolean activo) {
        // Se valida aqui y no al aplicar el evento para que el error aparezca
        // al armar el escenario y no a mitad de la simulación.
        if (despuesDePaquete < 1) {
            throw new IllegalArgumentException("El evento debe aplicarse despues del paquete 1 o posterior");
        }
        if (idNodoA == null || idNodoA.isBlank()) {
            throw new IllegalArgumentException("El id del nodo A del evento no puede ser vacio");
        }
        if (idNodoB == null || idNodoB.isBlank()) {
            throw new IllegalArgumentException("El id del nodo B del evento no puede ser vacio");
        }
        if (congestion < 0 || congestion > 1) {
            throw new IllegalArgumentException("La congestion debe estar entre 0 y 1");
        }
        this.despuesDePaquete = despuesDePaquete;
        this.idNodoA = idNodoA;
        this.idNodoB = idNodoB;
        this.congestion = congestion;
        this.activo = activo;
    }

    public int getDespuesDePaquete() { return despuesDePaquete; }
    public String getIdNodoA() { return idNodoA; }
    public String getIdNodoB() { return idNodoB; }
    public double getCongestion() { return congestion; }
    public boolean isActivo() { return activo; }
}
