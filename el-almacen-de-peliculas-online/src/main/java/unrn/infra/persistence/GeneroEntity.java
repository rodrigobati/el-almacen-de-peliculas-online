package unrn.infra.persistence;

import jakarta.persistence.*;

/**
 * Entidad JPA que persiste generos de peliculas.
 *
 * Funciona como catalogo normalizado de clasificaciones para filtrar, listar
 * categorias y relacionar cada pelicula con un genero consistente.
 */
@Entity
@Table(name = "genero")
public class GeneroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    public String nombre;

    /**
     * Inicializa una instancia de GeneroEntity con los datos necesarios.
     */
    protected GeneroEntity() {
    }

    /**
     * Inicializa una instancia de GeneroEntity con los datos necesarios.
     */
    public GeneroEntity(String nombre) {
        this.nombre = nombre;
    }
}
