package unrn.model;

import java.time.LocalDate;
import java.util.List;
import unrn.model.Actor;
import unrn.model.Director;
import unrn.model.Formato;
import unrn.model.Genero;
import unrn.model.Condicion;

public class Pelicula {
    static final String ERROR_TITULO = "El título no puede ser vacío";
    static final String ERROR_PRECIO = "El precio debe ser positivo";
    static final String ERROR_DIRECTORES = "Debe tener al menos un director";
    static final String ERROR_FORMATO = "El formato no puede ser vacío";
    static final String ERROR_GENERO = "El género no puede ser vacío";
    static final String ERROR_ACTORES = "Debe tener al menos un actor";
    static final String ERROR_FECHA = "La fecha de salida no puede ser nula";
    static final String ERROR_CONDICION = "La condición debe ser 'nuevo' o 'usado'";

    private final String titulo;
    private final Condicion condicion;
    private final List<Director> directores;
    private final double precio;
    private final Formato formato;
    private final Genero genero;
    private final String sinopsis;
    private final List<Actor> actores;
    private final String imagenUrl;
    private final LocalDate fechaSalida;

    /**
     * Devuelve el detalle completo de la película para mostrar en la vista.
     * Retorna un objeto inmutable con todos los datos relevantes.
     */
    public DetallePelicula detalleCompleto() {
        return new DetallePelicula(
                titulo,
                condicion,
                List.copyOf(directores),
                precio,
                formato,
                genero,
                sinopsis,
                List.copyOf(actores),
                imagenUrl,
                fechaSalida);
    }

    public Pelicula(String titulo, Condicion condicion, List<Director> directores, double precio, Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida) {
        assertTitulo(titulo);
        assertCondicion(condicion);
        assertDirectores(directores);
        assertPrecio(precio);
        assertFormato(formato);
        assertGenero(genero);
        assertActores(actores);
        assertFecha(fechaSalida);
        this.titulo = titulo;
        this.condicion = condicion;
        this.directores = List.copyOf(directores);
        this.precio = precio;
        this.formato = formato;
        this.genero = genero;
        this.sinopsis = sinopsis;
        this.actores = List.copyOf(actores);
        this.imagenUrl = imagenUrl;
        this.fechaSalida = fechaSalida;
    }

    private void assertTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new RuntimeException(ERROR_TITULO);
        }
    }

    private void assertCondicion(Condicion condicion) {
        if (condicion == null) {
            throw new RuntimeException(ERROR_CONDICION);
        }
    }

    private void assertDirectores(List<Director> directores) {
        if (directores == null || directores.isEmpty()) {
            throw new RuntimeException(ERROR_DIRECTORES);
        }
    }

    private void assertPrecio(double precio) {
        if (precio <= 0) {
            throw new RuntimeException(ERROR_PRECIO);
        }
    }

    private void assertFormato(Formato formato) {
        if (formato == null) {
            throw new RuntimeException(ERROR_FORMATO);
        }
    }

    private void assertGenero(Genero genero) {
        if (genero == null) {
            throw new RuntimeException(ERROR_GENERO);
        }
    }

    private void assertActores(List<Actor> actores) {
        if (actores == null || actores.isEmpty()) {
            throw new RuntimeException(ERROR_ACTORES);
        }
    }

    private void assertFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new RuntimeException(ERROR_FECHA);
        }
    }

    /** Value Object para el detalle completo de la película */
    public static final class DetallePelicula {
        public final String titulo;
        public final Condicion condicion;
        public final List<Director> directores;
        public final double precio;
        public final Formato formato;
        public final Genero genero;
        public final String sinopsis;
        public final List<Actor> actores;
        public final String imagenUrl;
        public final LocalDate fechaSalida;

        public DetallePelicula(String titulo, Condicion condicion, List<Director> directores, double precio, Formato formato,
                Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida) {
            this.titulo = titulo;
            this.condicion = condicion;
            this.directores = List.copyOf(directores);
            this.precio = precio;
            this.formato = formato;
            this.genero = genero;
            this.sinopsis = sinopsis;
            this.actores = List.copyOf(actores);
            this.imagenUrl = imagenUrl;
            this.fechaSalida = fechaSalida;
        }
    }
}
