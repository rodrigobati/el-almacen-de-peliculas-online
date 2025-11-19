package unrn.model;

import java.time.LocalDate;
import java.util.List;

public class Pelicula {
    static final String ERROR_TITULO = "El título no puede ser vacío";
    static final String ERROR_PRECIO = "El precio debe ser positivo";
    static final String ERROR_DIRECTORES = "Debe tener al menos un director";
    static final String ERROR_FORMATO = "El formato no puede ser vacío";
    static final String ERROR_GENERO = "El género no puede ser vacío";
    static final String ERROR_ACTORES = "Debe tener al menos un actor";
    static final String ERROR_FECHA = "La fecha de salida no puede ser nula";
    static final String ERROR_CONDICION = "La condición debe ser 'nuevo' o 'usado'";
    static final String ERROR_ID = "El id no puede ser nulo";

    private Long id;
    private String titulo;
    private List<Director> directores;
    private Condicion condicion;
    private double precio;
    private Formato formato;
    private Genero genero;
    private String sinopsis;
    private List<Actor> actores;
    private String imagenUrl;
    private LocalDate fechaSalida;
    private int rating; // Nuevo campo rating con valor por defecto 0
    private Double ratingPromedio; // Promedio de ratings de la comunidad
    private Integer totalRatings; // Cantidad total de ratings recibidos

    // El método que devolvía el DTO se eliminó para mantener el modelo desacoplado
    public Pelicula(String titulo, Condicion condicion, List<Director> directores, double precio, Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating) {
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
        this.rating = rating; // Inicialización del nuevo campo rating
    }

    public Pelicula(Long id, String titulo, Condicion condicion, List<Director> directores, double precio,
            Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating) {
        aasertId(id);
        assertTitulo(titulo);
        assertCondicion(condicion);
        assertDirectores(directores);
        assertPrecio(precio);
        assertFormato(formato);
        assertGenero(genero);
        assertActores(actores);
        assertFecha(fechaSalida);
        this.id = id;
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
        this.rating = rating;
    }

    private void aasertId(Long id) {
        if (id == null) {
            throw new RuntimeException(ERROR_ID);
        }
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

    // Métodos de lectura intencionales para uso por capas superiores (DTOs, vistas,
    // etc.)
    public Long id() {
        return id;
    }

    public String titulo() {
        return titulo;
    }

    public Condicion condicion() {
        return condicion;
    }

    public List<Director> directores() {
        return List.copyOf(directores);
    }

    public double precio() {
        return precio;
    }

    public Formato formato() {
        return formato;
    }

    public Genero genero() {
        return genero;
    }

    public String sinopsis() {
        return sinopsis;
    }

    public List<Actor> actores() {
        return List.copyOf(actores);
    }

    public String imagenUrl() {
        return imagenUrl;
    }

    public LocalDate fechaSalida() {
        return fechaSalida;
    }

    public int rating() {
        return rating;
    }

    public Double ratingPromedio() {
        return ratingPromedio;
    }

    public Integer totalRatings() {
        return totalRatings;
    }

    public void actualizarDesde(Pelicula nuevaPelicula) {
        if (nuevaPelicula == null)
            throw new RuntimeException("La película no puede ser null");

        // Validar todos los campos usando los assert existentes
        assertTitulo(nuevaPelicula.titulo);
        assertCondicion(nuevaPelicula.condicion);
        assertDirectores(nuevaPelicula.directores);
        assertPrecio(nuevaPelicula.precio);
        assertFormato(nuevaPelicula.formato);
        assertGenero(nuevaPelicula.genero);
        assertActores(nuevaPelicula.actores);
        assertFecha(nuevaPelicula.fechaSalida);

        // Si todas las validaciones pasan, actualizar los campos
        this.titulo = nuevaPelicula.titulo;
        this.condicion = nuevaPelicula.condicion;
        this.directores = List.copyOf(nuevaPelicula.directores);
        this.precio = nuevaPelicula.precio;
        this.formato = nuevaPelicula.formato;
        this.genero = nuevaPelicula.genero;
        this.sinopsis = nuevaPelicula.sinopsis;
        this.actores = List.copyOf(nuevaPelicula.actores);
        this.imagenUrl = nuevaPelicula.imagenUrl;
        this.fechaSalida = nuevaPelicula.fechaSalida;
        this.rating = nuevaPelicula.rating;
    }

    public void actualizarRating(int nuevoRating) {
        if (nuevoRating < 0 || nuevoRating > 5) {
            throw new RuntimeException("El rating debe estar entre 0 y 5");
        }
        this.rating = nuevoRating;
    }

    /**
     * Actualiza el rating promedio y total de ratings desde la vertical Rating.
     * Este método se invoca cuando se recibe un evento de RabbitMQ.
     */
    public void actualizarRatingPromedio(double ratingPromedio, int totalRatings) {
        if (ratingPromedio < 0 || ratingPromedio > 10) {
            throw new RuntimeException("El rating promedio debe estar entre 0 y 10");
        }
        if (totalRatings < 0) {
            throw new RuntimeException("El total de ratings no puede ser negativo");
        }
        this.ratingPromedio = ratingPromedio;
        this.totalRatings = totalRatings;
    }

    // El DTO se movió a la capa `unrn.dto` y el modelo ya no lo contiene
}
