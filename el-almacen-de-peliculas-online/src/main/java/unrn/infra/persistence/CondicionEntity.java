package unrn.infra.persistence;

import jakarta.persistence.*;

/**
 * Entidad JPA para la condicion comercial de una pelicula.
 *
 * Guarda valores como nuevo o usado en una tabla normalizada para que PeliculaEntity
 * pueda referenciarlos por clave y mantener consistencia entre registros.
 */
@Entity
@Table(name = "condicion")
public class CondicionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Byte id;

    @Column(name = "nombre", nullable = false, unique = true, length = 20)
    public String nombre;

    /**
     * Inicializa una instancia de CondicionEntity con los datos necesarios.
     */
    protected CondicionEntity() {
    }

    /**
     * Inicializa una instancia de CondicionEntity con los datos necesarios.
     */
    public CondicionEntity(String nombre) {
        this.nombre = nombre;
    }
}
