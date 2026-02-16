package unrn.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import unrn.app.Application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@SpringBootTest(classes = Application.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
@AutoConfigureMockMvc(addFilters = false)
class AdminPersonasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("crear director exitoso retorna 201 con id y nombre")
    void crearDirectorExitoso_retorna201ConIdYNombre() throws Exception {
        // Setup
        String nombre = "Director API " + System.nanoTime();

        // Ejercitación y Verificación
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value(nombre));
    }

    @Test
    @DisplayName("crear director con nombre vacio retorna 400 con mensaje")
    void crearDirectorNombreVacio_retorna400ConMensaje() throws Exception {
        // Setup
        String payload = "{\"nombre\":\"  \"}";

        // Ejercitación y Verificación
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(DirectorService.ERROR_NOMBRE_REQUERIDO));
    }

    @Test
    @DisplayName("crear director duplicado case insensitive retorna 400")
    void crearDirectorDuplicadoCaseInsensitive_retorna400() throws Exception {
        // Setup
        String base = "Director Duplicado API " + System.nanoTime();
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + base.toLowerCase() + "\"}"))
                .andExpect(status().isCreated());

        // Ejercitación y Verificación
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + base.toUpperCase() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(DirectorService.ERROR_NOMBRE_DUPLICADO));
    }

    @Test
    @DisplayName("buscar directores por q filtra resultados")
    void buscarDirectoresPorQ_filtraResultados() throws Exception {
        // Setup
        String token = "QDirector" + System.nanoTime();
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + token + " Uno\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Otro Director " + System.nanoTime() + "\"}"))
                .andExpect(status().isCreated());

        // Ejercitación y Verificación
        mockMvc.perform(get("/api/admin/directores").param("q", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value(org.hamcrest.Matchers.containsString(token)));
    }

    @Test
    @DisplayName("listar directores con size retorna 200 y arreglo")
    void listarDirectoresConSize_retorna200YArreglo() throws Exception {
        // Setup
        String nombre = "Director Listado " + System.nanoTime();
        mockMvc.perform(post("/api/admin/directores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated());

        // Ejercitación y Verificación
        mockMvc.perform(get("/api/admin/directores").param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("crear actor exitoso retorna 201 con id y nombre")
    void crearActorExitoso_retorna201ConIdYNombre() throws Exception {
        // Setup
        String nombre = "Actor API " + System.nanoTime();

        // Ejercitación y Verificación
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value(nombre));
    }

    @Test
    @DisplayName("crear actor con nombre vacio retorna 400 con mensaje")
    void crearActorNombreVacio_retorna400ConMensaje() throws Exception {
        // Setup
        String payload = "{\"nombre\":\"  \"}";

        // Ejercitación y Verificación
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ActorService.ERROR_NOMBRE_REQUERIDO));
    }

    @Test
    @DisplayName("crear actor duplicado case insensitive retorna 400")
    void crearActorDuplicadoCaseInsensitive_retorna400() throws Exception {
        // Setup
        String base = "Actor Duplicado API " + System.nanoTime();
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + base.toLowerCase() + "\"}"))
                .andExpect(status().isCreated());

        // Ejercitación y Verificación
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + base.toUpperCase() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ActorService.ERROR_NOMBRE_DUPLICADO));
    }

    @Test
    @DisplayName("buscar actores por q filtra resultados")
    void buscarActoresPorQ_filtraResultados() throws Exception {
        // Setup
        String token = "QActor" + System.nanoTime();
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + token + " Uno\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Otro Actor " + System.nanoTime() + "\"}"))
                .andExpect(status().isCreated());

        // Ejercitación y Verificación
        mockMvc.perform(get("/api/admin/actores").param("q", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value(org.hamcrest.Matchers.containsString(token)));
    }

    @Test
    @DisplayName("listar actores con size retorna 200 y arreglo")
    void listarActoresConSize_retorna200YArreglo() throws Exception {
        // Setup
        String nombre = "Actor Listado " + System.nanoTime();
        mockMvc.perform(post("/api/admin/actores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated());

        // Ejercitación y Verificación
        mockMvc.perform(get("/api/admin/actores").param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("buscar actores sin coincidencias retorna arreglo vacio")
    void buscarActoresSinCoincidencias_retornaArregloVacio() throws Exception {
        // Setup
        String q = "NoExisteActorApi_" + System.nanoTime();

        // Ejercitación y Verificación
        mockMvc.perform(get("/api/admin/actores").param("q", q).param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }
}
