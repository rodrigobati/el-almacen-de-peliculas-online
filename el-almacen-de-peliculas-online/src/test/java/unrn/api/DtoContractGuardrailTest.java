package unrn.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import unrn.dto.DetallePeliculaDTO;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,"
                +
                "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration"
})
@ActiveProfiles("test")
@ContextConfiguration(classes = unrn.app.Application.class)
public class DtoContractGuardrailTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("DetallePeliculaDTO_serialization_mandatoryFieldsAndTypes")
    void DetallePeliculaDTO_serialization_mandatoryFieldsAndTypes() {
        DetallePeliculaDTO sample = new DetallePeliculaDTO(
                1L,
                "Titulo ejemplo",
                "NUEVO",
                List.of("Director 1"),
                123.45,
                "DVD",
                "DRAMA",
                "Sinopsis ejemplo",
                List.of("Actor 1", "Actor 2"),
                "http://example.org/img.png",
                LocalDate.of(2020, 1, 1),
                5,
                4.5,
                10);

        JsonNode node = objectMapper.valueToTree(sample);

        // Mandatory top-level fields (conservative set)
        assertTrue(node.has("id"), "id must be present");
        assertTrue(node.get("id").isNumber(), "id must be numeric");

        assertTrue(node.has("titulo"), "titulo must be present");
        assertTrue(node.get("titulo").isTextual(), "titulo must be a string");

        assertTrue(node.has("precio"), "precio must be present");
        assertTrue(node.get("precio").isNumber(), "precio must be numeric");

        assertTrue(node.has("directores"), "directores must be present");
        assertTrue(node.get("directores").isArray(), "directores must be an array");

        assertTrue(node.has("actores"), "actores must be present");
        assertTrue(node.get("actores").isArray(), "actores must be an array");

        assertTrue(node.has("fechaSalida"), "fechaSalida must be present");
        assertTrue(node.get("fechaSalida").isTextual() || node.get("fechaSalida").isNumber(),
                "fechaSalida must be textual ISO date or numeric timestamp");

        assertTrue(node.has("rating"), "rating must be present");
        assertTrue(node.get("rating").isNumber(), "rating must be numeric");

        // Optional numeric fields that may be null: ratingPromedio, totalRatings
        if (node.has("ratingPromedio") && !node.get("ratingPromedio").isNull()) {
            assertTrue(node.get("ratingPromedio").isNumber(), "ratingPromedio must be numeric when present");
        }

        if (node.has("totalRatings") && !node.get("totalRatings").isNull()) {
            assertTrue(node.get("totalRatings").isNumber(), "totalRatings must be numeric when present");
        }
    }
}
