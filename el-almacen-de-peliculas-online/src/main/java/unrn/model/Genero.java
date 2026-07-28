package unrn.model;

/**
 * Modela el genero comercial o narrativo con el que se clasifica una pelicula.
 *
 * El catalogo lo usa como criterio de agrupacion, busqueda y exposicion de
 * categorias publicas. Mantenerlo como objeto de dominio permite validar el
 * nombre antes de que llegue a persistencia o a las respuestas HTTP.
 */
public class Genero {
    static final String ERROR_NOMBRE = "El gÃ©nero no puede ser vacÃ­o";
    private final String nombre;

    /**
     * Inicializa una instancia de Genero con los datos necesarios.
     */
    public Genero(String nombre) {
        assertNombre(nombre);
        this.nombre = nombre;
    }

    /**
     * Exige que el genero tenga un nombre no vacio.
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
