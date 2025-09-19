package unrn.infra.persistence;

import jakarta.persistence.*;
import jakarta.persistence.AccessType;

@Entity
@Table(name = "actor", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
@Access(AccessType.FIELD)
public class ActorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String nombre;

    protected ActorEntity() {
    } // JPA

    public ActorEntity(String nombre) {
        this.nombre = nombre;
    }

    // Sin getters/setters. Se accede por campo (JPA) y desde el mismo paquete.
}
