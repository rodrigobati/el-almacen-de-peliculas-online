package unrn.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import unrn.app.Application;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
@AutoConfigureMockMvc(addFilters = false)
class AdminPeliculasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("listarAdmin con datos devuelve PageResponse con items y total correctos")
    void listarAdmin_conDatos_devuelvePageResponseConItemsYTotalCorrectos() throws Exception {
        // Setup: crear director y actor y una pelicula
        String nombreDir = "Dir " + System.nanoTime();
        var dirResult = mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombreDir + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        String nombreAct = "Act " + System.nanoTime();
        var actResult = mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombreAct + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        // Ejercitación: crear una pelicula usando ids 1 (DB is clean per test so these
        // are first)
        String peliculaJson = "{\"titulo\":\"Peli Test\",\"condicion\":\"NUEVO\",\"directoresIds\":[1],\"precio\":100.0,\"formato\":\"DVD\",\"genero\":\"DRAMA\",\"sinopsis\":\"x\",\"actoresIds\":[1],\"imagenUrl\":\"\",\"fechaSalida\":\"2020-01-01\",\"rating\":5}";
        mockMvc.perform(post("/api/admin/peliculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(peliculaJson))
                .andExpect(status().isCreated());

        // Verificación: obtener la pagina admin y comprobar estructura
        mockMvc.perform(get("/api/admin/peliculas").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    @DisplayName("listarAdmin con page size devuelve cantidad esperada")
    void listarAdmin_conPageSize_devuelveCantidadEsperada() throws Exception {
        // Setup: crear director y actor
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"D1\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"A1\"}"))
                .andExpect(status().isCreated());

        // crear 3 peliculas
        for (int i = 0; i < 3; i++) {
            String peliculaJson = "{\"titulo\":\"Peli " + i
                    + "\",\"condicion\":\"NUEVO\",\"directoresIds\":[1],\"precio\":10.0,\"formato\":\"DVD\",\"genero\":\"DRAMA\",\"sinopsis\":\"x\",\"actoresIds\":[1],\"imagenUrl\":\"\",\"fechaSalida\":\"2020-01-01\",\"rating\":5}";
            mockMvc.perform(post("/api/admin/peliculas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(peliculaJson))
                    .andExpect(status().isCreated());
        }

        // size=2 should return items length 2 on first page
        mockMvc.perform(get("/api/admin/peliculas").param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(2)))
                .andExpect(jsonPath("$.total").isNumber());
    }

    @Test
    @DisplayName("listarAdmin con sort desc respeta orden")
    void listarAdmin_conSortDesc_respetaOrden() throws Exception {
        // Setup: director y actor
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"D2\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"A2\"}"))
                .andExpect(status().isCreated());

        // crear peliculas con titulos A, B
        String pA = "{\"titulo\":\"AAA\",\"condicion\":\"NUEVO\",\"directoresIds\":[1],\"precio\":10.0,\"formato\":\"DVD\",\"genero\":\"DRAMA\",\"sinopsis\":\"x\",\"actoresIds\":[1],\"imagenUrl\":\"\",\"fechaSalida\":\"2020-01-01\",\"rating\":5}";
        String pB = "{\"titulo\":\"ZZZ\",\"condicion\":\"NUEVO\",\"directoresIds\":[1],\"precio\":10.0,\"formato\":\"DVD\",\"genero\":\"DRAMA\",\"sinopsis\":\"x\",\"actoresIds\":[1],\"imagenUrl\":\"\",\"fechaSalida\":\"2020-01-01\",\"rating\":5}";
        mockMvc.perform(post("/api/admin/peliculas").contentType(MediaType.APPLICATION_JSON).content(pA))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/admin/peliculas").contentType(MediaType.APPLICATION_JSON).content(pB))
                .andExpect(status().isCreated());

        // request with sort=titulo & asc=false should return ZZZ first
        mockMvc.perform(get("/api/admin/peliculas").param("sort", "titulo").param("asc", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].titulo").value("ZZZ"));
    }
}
