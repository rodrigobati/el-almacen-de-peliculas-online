package unrn.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import unrn.app.Application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ValidationErrorContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.execute("DELETE FROM pelicula_actor");
        jdbcTemplate.execute("DELETE FROM pelicula_director");
        jdbcTemplate.execute("DELETE FROM pelicula");
        jdbcTemplate.execute("DELETE FROM actor");
        jdbcTemplate.execute("DELETE FROM director");
        jdbcTemplate.execute("DELETE FROM condicion");
        jdbcTemplate.execute("DELETE FROM formato");
        jdbcTemplate.execute("DELETE FROM genero");
    }

    @Test
    @DisplayName("listarPublico_pageNegativo_devuelveValidationError")
    void listarPublico_pageNegativo_devuelveValidationError() throws Exception {
        mockMvc.perform(get("/peliculas")
                .param("page", "-1")
                .param("size", "12")
                .param("sort", "titulo")
                .param("asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.details.field").value("page"))
                .andExpect(jsonPath("$.details.value").value(-1));
    }

    @Test
    @DisplayName("listarPublico_sizeCero_devuelveValidationError")
    void listarPublico_sizeCero_devuelveValidationError() throws Exception {
        mockMvc.perform(get("/peliculas")
                .param("page", "0")
                .param("size", "0")
                .param("sort", "titulo")
                .param("asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SIZE"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.details.field").value("size"))
                .andExpect(jsonPath("$.details.value").value(0))
                .andExpect(jsonPath("$.details.rule").isString());
    }

    @Test
    @DisplayName("listarPublico_sizeExcesivo_devuelveValidationError")
    void listarPublico_sizeExcesivo_devuelveValidationError() throws Exception {
        mockMvc.perform(get("/peliculas")
                .param("page", "0")
                .param("size", "101")
                .param("sort", "titulo")
                .param("asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SIZE"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.details.field").value("size"))
                .andExpect(jsonPath("$.details.value").value(101))
                .andExpect(jsonPath("$.details.rule").value("1..100"));
    }

    @Test
    @DisplayName("listarPublico_sortInvalido_devuelveValidationError")
    void listarPublico_sortInvalido_devuelveValidationError() throws Exception {
        mockMvc.perform(get("/peliculas")
                .param("page", "0")
                .param("size", "12")
                .param("sort", "campoNoPermitido")
                .param("asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.details.field").value("sort"));
    }
}
