package unrn.model;

public class Actor {
    static final String ERROR_NOMBRE = "El nombre del actor no puede ser vacío";
    private final String nombre;

    public Actor(String nombre) {
        assertNombre(nombre);
        this.nombre = nombre;
    }

    private void assertNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException(ERROR_NOMBRE);
        }
    }

    public String nombre() {
        return nombre;
    }
}
