package unrn.infra.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "formato")
public class FormatoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Short id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    public String nombre;

    protected FormatoEntity() {
    }

    public FormatoEntity(String nombre) {
        this.nombre = nombre;
    }
}
