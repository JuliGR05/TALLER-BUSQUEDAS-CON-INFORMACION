package conmutacion;

public class Main {

    /**
     * Red base: 4 routers de nucleo (R1-R4) y 3 usuarios de borde (A, B, C).
     * Cada usuario se conecta a dos routers de nucleo, asi hay varios caminos
     * posibles entre cualquier origen y destino y las rutas pueden cambiar.
     */
    public static Red construirRed() {
        Red red = new Red();

        // Nodos de nucleo
        red.agregarNodo("R1", ZonaRed.NUCLEO);
        red.agregarNodo("R2", ZonaRed.NUCLEO);
        red.agregarNodo("R3", ZonaRed.NUCLEO);
        red.agregarNodo("R4", ZonaRed.NUCLEO);

        // Usuarios (nodos de borde)
        red.agregarNodo("A", ZonaRed.BORDE);
        red.agregarNodo("B", ZonaRed.BORDE);
        red.agregarNodo("C", ZonaRed.BORDE);

        // Enlaces de nucleo: latencia baja, caida baja, throughput alto
        //              idA   idB   latenciaMs probCaida throughputMbps
        red.conectar("R1", "R2", 5, 0.01, 500);
        red.conectar("R1", "R3", 6, 0.01, 500);
        red.conectar("R2", "R3", 4, 0.01, 500);
        red.conectar("R2", "R4", 6, 0.01, 500);
        red.conectar("R3", "R4", 5, 0.01, 500);

        // Enlaces de borde: peor latencia, mas probabilidad de caida, menor throughput
        red.conectar("A", "R1", 10, 0.05, 100);
        red.conectar("A", "R2", 12, 0.05, 80);
        red.conectar("B", "R3", 10, 0.05, 100);
        red.conectar("B", "R4", 12, 0.05, 80);
        red.conectar("C", "R2", 10, 0.05, 100);
        red.conectar("C", "R4", 12, 0.05, 80);

        return red;
    }

    public static void main(String[] args) {
        Red red = construirRed();

        System.out.println("Nodos:");
        for (Nodo n : red.getNodos()) {
            System.out.println("  " + n.getId() + " (" + n.getZona() + ")");
        }
        System.out.println("Enlaces:");
        for (Enlace e : red.getEnlaces()) {
            System.out.printf("  %-6s %-6s lat=%4.1f ms  caida=%.2f  thr=%5.0f Mbps  peso=%.2f%n",
                    e, e.getZona(), e.getLatenciaMs(), e.getProbCaida(),
                    e.getThroughputMbps(), e.peso());
        }
        // TODO (#26, manana): escenarios A, B y C
    }
}
