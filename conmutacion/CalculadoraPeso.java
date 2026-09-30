package conmutacion;

public class CalculadoraPeso {

    //Constantes: ajustables en un solo lugar
    public static final double FACTOR_BORDE = 2.0;
    public static final double PESO_CAIDA = 100.0;
    public static final double PESO_THROUGHPUT = 100.0;

    public static double calcular(Enlace e) {
        //1.Enlace caído: nunca se debe usar
        if(!e.isActivo()){
            return Double.POSITIVE_INFINITY;
        }

        //2. Factor según la zona
        double factorZona = 1.0;
        if (e.getZona() == ZonaRed.BORDE) {
            factorZona = FACTOR_BORDE;
        }

        // 3. Los tres términos, uno por uno
        double retardo = e.getLatenciaMs() * (1 + e.getCongestion());
        double penalCaida = PESO_CAIDA * e.getProbCaida();
        double penalThroughput = PESO_THROUGHPUT / e.getThroughputMbps();

        // 4. Se suman y se multiplican por el factor de zona
        return factorZona * (retardo + penalCaida + penalThroughput);
    }
    
}
