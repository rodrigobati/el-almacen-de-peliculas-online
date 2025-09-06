package unrn.model;

public class Director {
    static final String ERROR_NOMBRE = "El nombre del director no puede ser vacío";
    private final String nombre;

    public Director(String nombre) {
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
