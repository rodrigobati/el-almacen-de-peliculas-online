package unrn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unrn.infra.persistence.PeliculaRepository;

import java.util.List;

/**
 * Controlador publico para consultar las categorias disponibles del catalogo.
 *
 * Se usa por el frontend para armar filtros o navegacion por genero sin exponer
 * detalles internos de persistencia. Toma la informacion desde el repositorio de
 * peliculas y responde solo los nombres visibles de categoria.
 */
@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final PeliculaRepository repo;

    /**
     * Inicializa una instancia de CategoriaController con los datos necesarios.
     */
    public CategoriaController(PeliculaRepository repo) {
        this.repo = repo;
    }

    /**
     * Devuelve los generos disponibles para filtros del catalogo publico.
     */
    @GetMapping
    public ResponseEntity<List<String>> listar() {
        var generos = repo.listarGeneros();
        return ResponseEntity.ok(generos);
    }
}
