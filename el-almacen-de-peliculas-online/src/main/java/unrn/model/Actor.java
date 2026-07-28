package unrn.model;

/**
 * Representa a un actor dentro del dominio del catalogo.
 *
 * Se usa para describir el reparto de una pelicula sin mezclar datos de persistencia
 * ni contratos de API. Su responsabilidad principal es conservar un nombre valido
 * y ofrecerlo como valor de negocio para busquedas, filtros y DTOs.
 */
public class Actor {
    static final String ERROR_NOMBRE = "El nombre del actor no puede ser vacÃ­o";
    private final String nombre;

    /**
     * Inicializa una instancia de Actor con los datos necesarios.
     */
    public Actor(String nombre) {
        assertNombre(nombre);
        this.nombre = nombre;
    }

    /**
     * Exige que el actor tenga un nombre no vacio.
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
