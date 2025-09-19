package unrn.infra.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "genero")
public class GeneroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    public String nombre;

    protected GeneroEntity() {
    }

    public GeneroEntity(String nombre) {
        this.nombre = nombre;
    }
}
