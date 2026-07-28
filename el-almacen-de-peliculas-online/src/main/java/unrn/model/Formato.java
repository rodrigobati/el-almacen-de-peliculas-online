package unrn.model;

/**
 * Describe el formato en el que una pelicula esta disponible para el cliente.
 *
 * Puede representar opciones como DVD, BluRay u otras variantes que el catalogo
 * quiera publicar. Se mantiene como valor de dominio para validar el tipo y usarlo
 * en filtros, administracion y presentacion de peliculas.
 */
public class Formato {
    static final String ERROR_TIPO = "El formato no puede ser vacÃ­o";
    private final String tipo;

    /**
     * Inicializa una instancia de Formato con los datos necesarios.
     */
    public Formato(String tipo) {
        assertTipo(tipo);
        this.tipo = tipo;
    }

    /**
     * Exige que el formato tenga un tipo no vacio.
     */
    private void assertTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new RuntimeException(ERROR_TIPO);
        }
    }

    /**
     * Devuelve el valor de tipo.
     */
    public String tipo() {
        return tipo;
    }
}
