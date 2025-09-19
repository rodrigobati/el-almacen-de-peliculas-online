package unrn.infra.persistence;

import jakarta.persistence.*;
import jakarta.persistence.AccessType;

@Entity
@Table(name = "director", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
@Access(AccessType.FIELD)
public class DirectorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String nombre;

    protected DirectorEntity() {
    } // JPA

    public DirectorEntity(String nombre) {
        this.nombre = nombre;
    }

    // Sin getters/setters.
}
