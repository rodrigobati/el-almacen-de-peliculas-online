package unrn.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import unrn.app.Application;
import unrn.config.JwtRoleConverter;
import unrn.dto.PeliculaRequest;
import unrn.event.movie.MovieEventPublisher;
import unrn.service.ActorService;
import unrn.service.DirectorService;
import unrn.service.PeliculaService;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPeliculasContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DirectorService directorService;

    @Autowired
    private ActorService actorService;

    @Autowired
    private PeliculaService peliculaService;

    @MockBean
    private MovieEventPublisher movieEventPublisher;

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
    @DisplayName("listarAdmin devuelvePageResponseConCamposObligatorios")
    void listarAdmin_devuelvePageResponseConCamposObligatorios() throws Exception {
        // Setup: Preparar el escenario
        crearPelicula("Admin Contract 1", 55.0);
        crearPelicula("Admin Contract 2", 65.0);

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("page", "0")
                .param("size", "2")
                .param("sort", "titulo")
                .param("asc", "true"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.items[0].stockDisponible").isNumber())
                .andExpect(jsonPath("$.items[0].version").isNumber());
    }

    @Test
    @DisplayName("listarAdmin sinResultados devuelveTotalPagesEnCero")
    void listarAdmin_sinResultados_devuelveTotalPagesEnCero() throws Exception {
        // Setup: Preparar el escenario

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("q", "NO_EXISTE_EN_CATALOGO")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "titulo")
                .param("asc", "true"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    @DisplayName("listarAdmin conSortTituloDesc respetaOrden")
    void listarAdmin_conSortTituloDesc_respetaOrden() throws Exception {
        // Setup: Preparar el escenario
        crearPelicula("AAA", 10.0);
        crearPelicula("ZZZ", 10.0);

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("sort", "titulo")
                .param("asc", "false")
                .param("page", "0")
                .param("size", "2"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].titulo").value("ZZZ"));
    }

    @Test
    @DisplayName("listarAdmin pageNegativo devuelve400")
    void listarAdmin_pageNegativo_devuelve400() throws Exception {
        // Setup: Preparar el escenario

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("page", "-1")
                .param("size", "12")
                .param("sort", "titulo")
                .param("asc", "true"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PAGE"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("listarAdmin sizeCero devuelve400")
    void listarAdmin_sizeCero_devuelve400() throws Exception {
        // Setup: Preparar el escenario

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("page", "0")
                .param("size", "0")
                .param("sort", "titulo")
                .param("asc", "true"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SIZE"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("listarAdmin sizeExcesivo devuelve400")
    void listarAdmin_sizeExcesivo_devuelve400() throws Exception {
        // Setup: Preparar el escenario

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("page", "0")
                .param("size", "101")
                .param("sort", "titulo")
                .param("asc", "true"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SIZE"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("listarAdmin sortInvalido devuelve400")
    void listarAdmin_sortInvalido_devuelve400() throws Exception {
        // Setup: Preparar el escenario

        // Ejercitación: Ejecutar la acción a probar
        mockMvc.perform(get("/api/admin/peliculas")
                .with(jwtAdminClaims())
                .param("page", "0")
                .param("size", "12")
                .param("sort", "campoNoPermitido")
                .param("asc", "true"))
                // Verificación: Verificar el resultado esperado
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SORT"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    @DisplayName("actualizarStock conVersionActual actualizaStockEIncrementaVersion")
    void actualizarStock_conVersionActual_actualizaStockEIncrementaVersion() throws Exception {
        crearPelicula("Stock Versioned", 40.0);
        Long id = idPeliculaPorTitulo("Stock Versioned");
        Long version = versionPelicula(id);

        mockMvc.perform(patch("/api/admin/peliculas/{id}/stock", id)
                .with(jwtAdminClaims())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stockDisponible\":7,\"version\":" + version + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.stockDisponible").value(7.0))
                .andExpect(jsonPath("$.version").value(version + 1));

        BigDecimal stockPersistido = jdbcTemplate.queryForObject(
                "SELECT stock_disponible FROM pelicula WHERE id = ?",
                BigDecimal.class,
                id);
        assertEquals(0, new BigDecimal("7.00").compareTo(stockPersistido));
    }

    @Test
    @DisplayName("actualizarStock conVersionVieja devuelve409")
    void actualizarStock_conVersionVieja_devuelve409() throws Exception {
        crearPelicula("Stock Conflict", 40.0);
        Long id = idPeliculaPorTitulo("Stock Conflict");
        Long version = versionPelicula(id);

        mockMvc.perform(patch("/api/admin/peliculas/{id}/stock", id)
                .with(jwtAdminClaims())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stockDisponible\":7,\"version\":" + (version + 10) + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOCK_VERSION_CONFLICT"))
                .andExpect(jsonPath("$.details.peliculaId").value(id))
                .andExpect(jsonPath("$.details.currentVersion").value(version));
    }

    private void crearPelicula(String titulo, double precio) {
        var director = directorService.crear("Director " + titulo);
        var actor = actorService.crear("Actor " + titulo);

        peliculaService.crearPelicula(new PeliculaRequest(
                titulo,
                "NUEVO",
                List.of(director.id()),
                precio,
                "DVD",
                "DRAMA",
                "Sinopsis " + titulo,
                List.of(actor.id()),
                "",
                LocalDate.of(2020, 1, 1),
                5));
    }

    private Long idPeliculaPorTitulo(String titulo) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM pelicula WHERE titulo = ?",
                Long.class,
                titulo);
    }

    private Long versionPelicula(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT version FROM pelicula WHERE id = ?",
                Long.class,
                id);
    }

    private RequestPostProcessor jwtAdminClaims() {
        return jwt()
                .jwt(token -> token
                        .claim("sub", "admin-contract")
                        .claim("azp", "web")
                        .claim("realm_access", Map.of("roles", List.of("admin")))
                        .claim("resource_access", Map.of("web", Map.of("roles", List.of("admin")))))
                .authorities(new JwtRoleConverter("web"));
    }
}
