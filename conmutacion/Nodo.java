package conmutacion;

public class Nodo {
    private final String id;
    private final ZonaRed zona;

    public Nodo(String id, ZonaRed zona) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del nodo no puede ser vacio");
        }
        if (zona == null) {
            throw new IllegalArgumentException("La zona del nodo no puede ser null");
        }
        this.id = id;
        this.zona = zona;
    }

    public String getId() { return id; }
    public ZonaRed getZona() { return zona; }

    @Override
    public String toString() {
        return id;
    }
}
