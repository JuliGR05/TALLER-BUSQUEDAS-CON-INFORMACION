package src.conmutacion;

/** Enlace no dirigido entre dos nodos. */
public class Enlace {
    private final Nodo nodoA;
    private final Nodo nodoB;
    private final double latenciaMs;
    private double congestion;          // 0 a 1, empieza en 0
    private final double probCaida;     // 0 a 1
    private final double throughputMbps;
    private boolean activo;             // empieza en true

    public Enlace(Nodo nodoA, Nodo nodoB, double latenciaMs,
                  double probCaida, double throughputMbps) {
        if (nodoA == null || nodoB == null) {
            throw new IllegalArgumentException("Los extremos del enlace no pueden ser null");
        }
        if (nodoA == nodoB) {
            throw new IllegalArgumentException("Un enlace no puede conectar un nodo consigo mismo");
        }
        if (latenciaMs < 0) {
            throw new IllegalArgumentException("La latencia no puede ser negativa");
        }
        if (probCaida < 0 || probCaida > 1) {
            throw new IllegalArgumentException("probCaida debe estar entre 0 y 1");
        }
        if (throughputMbps <= 0) {
            throw new IllegalArgumentException("El throughput debe ser mayor que 0");
        }
        this.nodoA = nodoA;
        this.nodoB = nodoB;
        this.latenciaMs = latenciaMs;
        this.probCaida = probCaida;
        this.throughputMbps = throughputMbps;
        this.congestion = 0.0;
        this.activo = true;
    }

    public Nodo getNodoA() { return nodoA; }
    public Nodo getNodoB() { return nodoB; }

    /** BORDE si alguno de los dos extremos es de borde; si no, NUCLEO. */
    public ZonaRed getZona() {
        if (nodoA.getZona() == ZonaRed.BORDE || nodoB.getZona() == ZonaRed.BORDE) {
            return ZonaRed.BORDE;
        }
        return ZonaRed.NUCLEO;
    }

    /** Devuelve el nodo del otro lado del enlace. */
    public Nodo otroExtremo(Nodo n) {
        if (n == nodoA) return nodoB;
        if (n == nodoB) return nodoA;
        throw new IllegalArgumentException("El nodo " + n + " no es extremo del enlace " + this);
    }

    /** Peso actual del enlace (Double.POSITIVE_INFINITY si esta caido). */
    public double peso() {
        return CalculadoraPeso.calcular(this);
    }

    public double getLatenciaMs() { return latenciaMs; }
    public double getCongestion() { return congestion; }
    public double getProbCaida() { return probCaida; }
    public double getThroughputMbps() { return throughputMbps; }
    public boolean isActivo() { return activo; }

    public void setCongestion(double congestion) {
        if (congestion < 0 || congestion > 1) {
            throw new IllegalArgumentException("La congestion debe estar entre 0 y 1");
        }
        this.congestion = congestion;
    }

    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return nodoA + "-" + nodoB;
    }
}
