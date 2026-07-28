package unrn.model;

import java.util.Objects;

/**
 * Representa una categoria de navegacion del catalogo, derivada del titulo visible.
 *
 * Se usa para agrupar o filtrar peliculas desde la mirada del usuario, normalmente
 * a partir del genero publicado. La igualdad ignora mayusculas para evitar duplicar
 * categorias que en negocio significan lo mismo.
 */
public final class Categoria {
    static final String ERROR_TITULO = "El tÃ­tulo de la categorÃ­a no puede ser vacÃ­o";
    private final String titulo;

    /**
     * Inicializa una instancia de Categoria con los datos necesarios.
     */
    public Categoria(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new RuntimeException(ERROR_TITULO);
        }
        this.titulo = titulo;
    }

    /**
     * Devuelve el valor de titulo.
     */
    public String titulo() {
        return titulo;
    }

    /**
     * Compara este objeto con otro segun su identidad de valor.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Categoria categoria = (Categoria) o;
        return titulo.equalsIgnoreCase(categoria.titulo);
    }

    /**
     * Calcula el hash consistente con equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(titulo.toLowerCase());
    }

    /**
     * Devuelve la representacion textual del objeto.
     */
    @Override
    public String toString() {
        return titulo;
    }
}
