package unrn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unrn.infra.persistence.PeliculaRepository;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final PeliculaRepository repo;

    public CategoriaController(PeliculaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<String>> listar() {
        var generos = repo.listarGeneros();
        return ResponseEntity.ok(generos);
    }
}
