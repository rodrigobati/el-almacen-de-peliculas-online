package unrn.infra.persistence;

import jakarta.persistence.*;
import unrn.model.Actor;

/**
 * Entidad JPA que persiste actores disponibles para asociar a peliculas.
 *
 * Mantiene el nombre unico en base de datos y sabe convertirse al objeto de dominio
 * Actor. Se usa en relaciones many-to-many de PeliculaEntity y en la API admin de
 * actores.
 */
@Entity
@Table(name = "actor", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
@Access(AccessType.FIELD)
public class ActorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String nombre;

    /**
     * Inicializa una instancia de ActorEntity con los datos necesarios.
     */
    protected ActorEntity() {
    } // JPA

    /**
     * Inicializa una instancia de ActorEntity con los datos necesarios.
     */
    public ActorEntity(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Convierte la entidad de persistencia al modelo de dominio.
     */
    public Actor asDomain() {
        return new Actor(this.nombre);
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

    // Sin getters/setters. Se accede por campo (JPA) y desde el mismo paquete.
}
