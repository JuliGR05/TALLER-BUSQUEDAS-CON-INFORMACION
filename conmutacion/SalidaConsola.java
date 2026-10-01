package conmutacion;

import java.util.List;

public class SalidaConsola {

    public static void imprimir(List<Paquete> paquetes) {
        System.out.printf("%5s | %-7s | %8s | %s%n", "Orden", "Paquete", "Llegada", "Ruta");
        System.out.println("------------------------------------------------");

        for (Paquete p : paquetes) {
            String nombre = "M" + p.getIdMensaje() + "-P" + p.getNumero();

            if (p.getRuta().isEmpty() || Double.isInfinite(p.getTiempoLlegada())) {
                // El paquete no pudo llegar
                System.out.printf("%5s | %-7s | %8s | %s%n",
                        "--", nombre, "no llega", "sin ruta disponible");
            } else {
                System.out.printf("%5d | %-7s | %8.1f | %s%n",
                        p.getOrdenLlegada(), nombre, p.getTiempoLlegada(),
                        formatearRuta(p.getRuta()));
            }
        }
    }

    // Convierte la lista de nodos en texto: A -> R1 -> B
    private static String formatearRuta(List<Nodo> ruta) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ruta.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(ruta.get(i).getId());
        }
        return sb.toString();
    }
}