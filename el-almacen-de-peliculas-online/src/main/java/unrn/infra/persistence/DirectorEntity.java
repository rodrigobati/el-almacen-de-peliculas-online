package unrn.infra.persistence;

import jakarta.persistence.*;
import unrn.model.Director;

/**
 * Entidad JPA que persiste directores reutilizables del catalogo.
 *
 * Representa la tabla director, asegura unicidad por nombre y ofrece conversion al
 * dominio Director para que servicios y DTOs no dependan de detalles de JPA.
 */
@Entity
@Table(name = "director", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
@Access(AccessType.FIELD)
public class DirectorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String nombre;

    /**
     * Inicializa una instancia de DirectorEntity con los datos necesarios.
     */
    protected DirectorEntity() {
    } // JPA

    /**
     * Inicializa una instancia de DirectorEntity con los datos necesarios.
     */
    public DirectorEntity(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Convierte la entidad de persistencia al modelo de dominio.
     */
    public Director asDomain() {
        return new Director(this.nombre);
    }

    /**
     * Devuelve el valor de id.
     */
    public Long id() {
        return this.id;
    }

    /**
     * Devuelve el valor de nombre.
     */
    public String nombre() {
        return this.nombre;
    }

}
