package unrn.model;

import java.util.Objects;

public final class Categoria {
    static final String ERROR_TITULO = "El título de la categoría no puede ser vacío";
    private final String titulo;

    public Categoria(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new RuntimeException(ERROR_TITULO);
        }
        this.titulo = titulo;
    }

    public String titulo() {
        return titulo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Categoria categoria = (Categoria) o;
        return titulo.equalsIgnoreCase(categoria.titulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo.toLowerCase());
    }

    @Override
    public String toString() {
        return titulo;
    }
}
