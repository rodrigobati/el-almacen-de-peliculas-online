package unrn.infra.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "condicion")
public class CondicionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Byte id;

    @Column(name = "nombre", nullable = false, unique = true, length = 20)
    public String nombre;

    protected CondicionEntity() {
    }

    public CondicionEntity(String nombre) {
        this.nombre = nombre;
    }
}
