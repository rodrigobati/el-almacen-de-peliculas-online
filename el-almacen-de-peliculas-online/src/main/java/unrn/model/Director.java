package unrn.model;

/**
 * Representa a un director dentro del dominio del catalogo.
 *
 * Las peliculas lo usan para expresar autoria o direccion y los servicios lo
 * resuelven desde identificadores administrativos. La clase mantiene la regla
 * basica de que el nombre exista y sea util para mostrar y filtrar catalogo.
 */
public class Director {
    static final String ERROR_NOMBRE = "El nombre del director no puede ser vacÃ­o";
    private final String nombre;

    /**
     * Inicializa una instancia de Director con los datos necesarios.
     */
    public Director(String nombre) {
        assertNombre(nombre);
        this.nombre = nombre;
    }

    /**
     * Exige que el director tenga un nombre no vacio.
     */
    private void assertNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new RuntimeException(ERROR_NOMBRE);
        }
    }

    /**
     * Devuelve el valor de nombre.
     */
    public String nombre() {
        return nombre;
    }
}
