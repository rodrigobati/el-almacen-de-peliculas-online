package unrn.infra.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeliculaEntityTest {
    @Test
    @DisplayName("Version cero se normaliza a uno en el dominio")
    void asDomain_versionCero_seNormalizaAUno() {
        // Setup: crear entidad con version cero
        var entity = new PeliculaEntity(
                "Titulo",
                new CondicionEntity("nuevo"),
                new BigDecimal("10.00"),
                new FormatoEntity("DVD"),
                new GeneroEntity("Accion"),
                "Sinopsis",
                "http://example.com/img.jpg",
                LocalDate.of(2020, 1, 1),
                List.of(new DirectorEntity("Director")),
                List.of(new ActorEntity("Actor")),
                3);
        entity.version = 0;

        // Ejercitacion: convertir a dominio
        var pelicula = entity.asDomain();

        // Verificacion: version normalizada
        assertEquals(1L, pelicula.version(), "La version del dominio debe ser uno");
    }
}
