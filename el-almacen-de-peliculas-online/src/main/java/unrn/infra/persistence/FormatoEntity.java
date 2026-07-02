package unrn.infra.persistence;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa los formatos disponibles del catalogo.
 *
 * Permite normalizar opciones como DVD o BluRay y reutilizarlas en muchas peliculas
 * sin duplicar texto libre en la tabla principal.
 */
@Entity
@Table(name = "formato")
public class FormatoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    public String nombre;

    /**
     * Inicializa una instancia de FormatoEntity con los datos necesarios.
     */
    protected FormatoEntity() {
    }

    /**
     * Inicializa una instancia de FormatoEntity con los datos necesarios.
     */
    public FormatoEntity(String nombre) {
        this.nombre = nombre;
    }
}
