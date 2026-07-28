package unrn.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Agregado principal del dominio de catalogo.
 *
 * Contiene los datos que definen una pelicula publicable: titulo, condicion,
 * directores, actores, precio, formato, genero, fechas, rating, stock y estado
 * activo. Tambien concentra reglas de negocio como validar datos obligatorios,
 * retirar una pelicula, actualizar rating y mantener version para cambios de stock.
 */
public class Pelicula {
    static final String ERROR_TITULO = "El tÃ­tulo no puede ser vacÃ­o";
    static final String ERROR_PRECIO = "El precio debe ser positivo";
    static final String ERROR_DIRECTORES = "Debe tener al menos un director";
    static final String ERROR_FORMATO = "El formato no puede ser vacÃ­o";
    static final String ERROR_GENERO = "El gÃ©nero no puede ser vacÃ­o";
    static final String ERROR_ACTORES = "Debe tener al menos un actor";
    static final String ERROR_FECHA = "La fecha de salida no puede ser nula";
    static final String ERROR_CONDICION = "La condiciÃ³n debe ser 'nuevo' o 'usado'";
    static final String ERROR_ID = "El id no puede ser nulo";
    static final String ERROR_VERSION_INVALIDA = "La versiÃ³n debe ser mayor a cero";
    static final String ERROR_PELICULA_INACTIVA = "La pelÃ­cula ya estÃ¡ inactiva";
    static final String ERROR_STOCK = "El stock no puede ser negativo";
    private static final BigDecimal STOCK_INICIAL = new BigDecimal("100.00");

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
    private int rating; // Campo rating con valor por defecto 0
    private Double ratingPromedio; // Promedio de ratings de la comunidad
    private Integer totalRatings; // Cantidad total de ratings recibidos
    private boolean activa;
    private long version;
    private BigDecimal stockDisponible;

    /**
     * El mÃ©todo que devolvÃ­a el DTO se eliminÃ³ para mantener el modelo desacoplado
     * Inicializa una pelicula nueva con stock inicial, activa y version inicial.
     */
    public Pelicula(String titulo, Condicion condicion, List<Director> directores, double precio, Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating) {
        this(titulo, condicion, directores, precio, formato, genero, sinopsis, actores, imagenUrl, fechaSalida, rating,
                true, 1L, STOCK_INICIAL);
    }

    /**
     * Inicializa una instancia de Pelicula con los datos necesarios.
     */
    private Pelicula(String titulo, Condicion condicion, List<Director> directores, double precio, Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating,
            boolean activa, long version, BigDecimal stockDisponible) {
        assertTitulo(titulo);
        assertCondicion(condicion);
        assertDirectores(directores);
        assertPrecio(precio);
        assertFormato(formato);
        assertGenero(genero);
        assertActores(actores);
        assertFecha(fechaSalida);
        assertVersion(version);
        assertStock(stockDisponible);
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
        this.rating = rating; // InicializaciÃ³n del nuevo campo rating
        this.activa = activa;
        this.version = version;
        this.stockDisponible = stockDisponible;
    }

    /**
     * Inicializa una instancia de Pelicula con los datos necesarios.
     */
    public Pelicula(Long id, String titulo, Condicion condicion, List<Director> directores, double precio,
            Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating) {
        this(id, titulo, condicion, directores, precio, formato, genero, sinopsis, actores, imagenUrl, fechaSalida,
                rating, true, 1L);
    }

    /**
     * Inicializa una instancia de Pelicula con los datos necesarios.
     */
    public Pelicula(Long id, String titulo, Condicion condicion, List<Director> directores, double precio,
            Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating,
            boolean activa, long version) {
        this(id, titulo, condicion, directores, precio, formato, genero, sinopsis, actores, imagenUrl, fechaSalida,
                rating, activa, version, STOCK_INICIAL);
    }

    /**
     * Inicializa una instancia de Pelicula con los datos necesarios.
     */
    public Pelicula(Long id, String titulo, Condicion condicion, List<Director> directores, double precio,
            Formato formato,
            Genero genero, String sinopsis, List<Actor> actores, String imagenUrl, LocalDate fechaSalida, int rating,
            boolean activa, long version, BigDecimal stockDisponible) {
        aasertId(id);
        assertTitulo(titulo);
        assertCondicion(condicion);
        assertDirectores(directores);
        assertPrecio(precio);
        assertFormato(formato);
        assertGenero(genero);
        assertActores(actores);
        assertFecha(fechaSalida);
        assertVersion(version);
        assertStock(stockDisponible);
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
        this.activa = activa;
        this.version = version;
        this.stockDisponible = stockDisponible;
    }

    /**
     * Valida que el identificador de una pelicula persistida no sea nulo.
     */
    private void aasertId(Long id) {
        if (id == null) {
            throw new RuntimeException(ERROR_ID);
        }
    }

    /**
     * Exige que la pelicula tenga un titulo no vacio.
     */
    private void assertTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new RuntimeException(ERROR_TITULO);
        }
    }

    /**
     * Exige que la pelicula tenga una condicion valida.
     */
    private void assertCondicion(Condicion condicion) {
        if (condicion == null) {
            throw new RuntimeException(ERROR_CONDICION);
        }
    }

    /**
     * Exige que la pelicula tenga al menos un director.
     */
    private void assertDirectores(List<Director> directores) {
        if (directores == null || directores.isEmpty()) {
            throw new RuntimeException(ERROR_DIRECTORES);
        }
    }

    /**
     * Exige que el precio de la pelicula sea mayor a cero.
     */
    private void assertPrecio(double precio) {
        if (precio <= 0) {
            throw new RuntimeException(ERROR_PRECIO);
        }
    }

    /**
     * Exige que la pelicula tenga un formato asociado.
     */
    private void assertFormato(Formato formato) {
        if (formato == null) {
            throw new RuntimeException(ERROR_FORMATO);
        }
    }

    /**
     * Exige que la pelicula tenga un genero asociado.
     */
    private void assertGenero(Genero genero) {
        if (genero == null) {
            throw new RuntimeException(ERROR_GENERO);
        }
    }

    /**
     * Exige que la pelicula tenga al menos un actor.
     */
    private void assertActores(List<Actor> actores) {
        if (actores == null || actores.isEmpty()) {
            throw new RuntimeException(ERROR_ACTORES);
        }
    }

    /**
     * Exige que la pelicula tenga fecha de salida.
     */
    private void assertFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new RuntimeException(ERROR_FECHA);
        }
    }

    /**
     * Exige que la version de concurrencia no sea negativa.
     */
    private void assertVersion(long version) {
        if (version < 0) {
            throw new RuntimeException(ERROR_VERSION_INVALIDA);
        }
    }

    /**
     * Exige que el stock disponible exista y no sea negativo.
     */
    private void assertStock(BigDecimal stockDisponible) {
        if (stockDisponible == null || stockDisponible.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException(ERROR_STOCK);
        }
    }

    /**
     * MÃ©todos de lectura intencionales para uso por capas superiores (DTOs, vistas,
     * etc.)
     * Devuelve el valor de id.
     */
    public Long id() {
        return id;
    }

    /**
     * Devuelve el valor de titulo.
     */
    public String titulo() {
        return titulo;
    }

    /**
     * Devuelve el valor de condicion.
     */
    public Condicion condicion() {
        return condicion;
    }

    /**
     * Devuelve el valor de directores.
     */
    public List<Director> directores() {
        return List.copyOf(directores);
    }

    /**
     * Devuelve el valor de precio.
     */
    public double precio() {
        return precio;
    }

    /**
     * Devuelve el valor de formato.
     */
    public Formato formato() {
        return formato;
    }

    /**
     * Devuelve el valor de genero.
     */
    public Genero genero() {
        return genero;
    }

    /**
     * Devuelve el valor de sinopsis.
     */
    public String sinopsis() {
        return sinopsis;
    }

    /**
     * Devuelve el valor de actores.
     */
    public List<Actor> actores() {
        return List.copyOf(actores);
    }

    /**
     * Devuelve el valor de imagenUrl.
     */
    public String imagenUrl() {
        return imagenUrl;
    }

    /**
     * Devuelve el valor de fechaSalida.
     */
    public LocalDate fechaSalida() {
        return fechaSalida;
    }

    /**
     * Devuelve el valor de rating.
     */
    public int rating() {
        return rating;
    }

    /**
     * Devuelve el valor de ratingPromedio.
     */
    public Double ratingPromedio() {
        return ratingPromedio;
    }

    /**
     * Devuelve el valor de totalRatings.
     */
    public Integer totalRatings() {
        return totalRatings;
    }

    /**
     * Devuelve el valor de activa.
     */
    public boolean activa() {
        return activa;
    }

    /**
     * Devuelve el valor de version.
     */
    public long version() {
        return version;
    }

    /**
     * Devuelve el valor de stockDisponible.
     */
    public BigDecimal stockDisponible() {
        return stockDisponible;
    }

    /**
     * Actualiza los datos editables desde otra pelicula validada.
     */
    public void actualizarDesde(Pelicula nuevaPelicula) {
        if (nuevaPelicula == null)
            throw new RuntimeException("La pelÃ­cula no puede ser null");

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

    /**
     * Marca la pelicula como inactiva e incrementa su version.
     */
    public void retirar() {
        assertActiva();
        this.activa = false;
        this.version += 1;
    }

    /**
     * Impide retirar una pelicula que ya esta inactiva.
     */
    private void assertActiva() {
        if (!this.activa) {
            throw new RuntimeException(ERROR_PELICULA_INACTIVA);
        }
    }

    /**
     * Actualiza el rating local validando su rango permitido.
     */
    public void actualizarRating(int nuevoRating) {
        if (nuevoRating < 0 || nuevoRating > 5) {
            throw new RuntimeException("El rating debe estar entre 0 y 5");
        }
        this.rating = nuevoRating;
    }

    /**
     * Actualiza el rating promedio y total de ratings desde la vertical Rating.
     * Este mÃ©todo se invoca cuando se recibe un evento de RabbitMQ.
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

    // El DTO se moviÃ³ a la capa `unrn.dto` y el modelo ya no lo contiene
}
