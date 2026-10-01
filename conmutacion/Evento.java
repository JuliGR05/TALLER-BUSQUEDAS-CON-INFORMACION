package conmutacion;

public class Evento {
    private final int despuesDePaquete;
    private final String idNodoA;
    private final String idNodoB;
    private final double congestion;
    private final boolean activo;

    public Evento(int despuesDePaquete, String idNodoA, String idNodoB,
                  double congestion, boolean activo) {
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