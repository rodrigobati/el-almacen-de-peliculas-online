package unrn.infra.persistence;

import jakarta.persistence.*;
import jakarta.persistence.AccessType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pelicula")
@NamedQueries({
        @NamedQuery(name = "PeliculaEntity.buscarPorTitulo", query = "SELECT p FROM PeliculaEntity p WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%'))"),
        @NamedQuery(name = "PeliculaEntity.buscarPorGenero", query = "SELECT p FROM PeliculaEntity p WHERE LOWER(p.genero) = LOWER(:genero)"),
        @NamedQuery(name = "PeliculaEntity.buscarPorActor", query = "SELECT p FROM PeliculaEntity p JOIN p.actores a WHERE LOWER(a.nombre) LIKE LOWER(CONCAT('%', :actor, '%'))"),
        @NamedQuery(name = "PeliculaEntity.buscarPorDirector", query = "SELECT p FROM PeliculaEntity p JOIN p.directores d WHERE LOWER(d.nombre) LIKE LOWER(CONCAT('%', :director, '%'))"),
        @NamedQuery(name = "PeliculaEntity.contarPorGenero", query = "SELECT COUNT(p) FROM PeliculaEntity p WHERE LOWER(p.genero) = LOWER(:genero)"),
        @NamedQuery(name = "PeliculaEntity.existePorTitulo", query = "SELECT COUNT(p) FROM PeliculaEntity p WHERE LOWER(p.titulo) = LOWER(:titulo)")
})
@Access(AccessType.FIELD)
public class PeliculaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String titulo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "condicion_id", nullable = false)
    CondicionEntity condicion;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal precio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "formato_id", nullable = false)
    FormatoEntity formato;

    @ManyToOne(optional = false)
    @JoinColumn(name = "genero_id", nullable = false)
    GeneroEntity genero;

    @Lob
    String sinopsis;

    @Column(name = "imagen_url", length = 500)
    String imagenUrl;

    @Column(name = "fecha_salida", nullable = false)
    LocalDate fechaSalida;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "pelicula_director", joinColumns = @JoinColumn(name = "pelicula_id"), inverseJoinColumns = @JoinColumn(name = "director_id"))
    List<DirectorEntity> directores = new ArrayList<>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "pelicula_actor", joinColumns = @JoinColumn(name = "pelicula_id"), inverseJoinColumns = @JoinColumn(name = "actor_id"))
    List<ActorEntity> actores = new ArrayList<>();

    @Column(name = "rating", nullable = false)
    int rating;

    @Column(name = "rating_promedio")
    Double ratingPromedio;

    @Column(name = "total_ratings")
    Integer totalRatings;

    protected PeliculaEntity() {
    } // JPA

    public PeliculaEntity(String titulo, CondicionEntity condicion, BigDecimal precio, FormatoEntity formato,
            GeneroEntity genero,
            String sinopsis, String imagenUrl, LocalDate fechaSalida,
            List<DirectorEntity> directores, List<ActorEntity> actores, int rating) {
        this.titulo = titulo;
        this.condicion = condicion;
        this.precio = precio;
        this.formato = formato;
        this.genero = genero;
        this.sinopsis = sinopsis;
        this.imagenUrl = imagenUrl;
        this.fechaSalida = fechaSalida;
        if (directores != null)
            this.directores.addAll(directores);
        if (actores != null)
            this.actores.addAll(actores);
        this.rating = rating;
        this.ratingPromedio = null;
        this.totalRatings = null;
    }

    public unrn.model.Pelicula asDomain() {
        var d = new java.util.ArrayList<unrn.model.Director>();
        for (var de : this.directores)
            d.add(new unrn.model.Director(de.nombre));
        var a = new java.util.ArrayList<unrn.model.Actor>();
        for (var ae : this.actores)
            a.add(new unrn.model.Actor(ae.nombre));
        var pelicula = new unrn.model.Pelicula(
                this.id,
                this.titulo,
                new unrn.model.Condicion(this.condicion.nombre),
                d,
                this.precio.doubleValue(),
                new unrn.model.Formato(this.formato.nombre),
                new unrn.model.Genero(this.genero.nombre),
                this.sinopsis,
                a,
                this.imagenUrl,
                this.fechaSalida,
                this.rating);

        // Si hay datos de rating comunitario, actualizarlos
        if (this.ratingPromedio != null && this.totalRatings != null) {
            pelicula.actualizarRatingPromedio(this.ratingPromedio, this.totalRatings);
        }

        return pelicula;
    }
}
