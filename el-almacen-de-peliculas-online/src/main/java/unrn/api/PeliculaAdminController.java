package unrn.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unrn.dto.PeliculaRequest;
import unrn.service.PeliculaService;

@RestController
@RequestMapping("/api/admin/peliculas")
public class PeliculaAdminController {

    private final PeliculaService peliculaService;

    public PeliculaAdminController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    // CREAR
    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody PeliculaRequest request) {
        peliculaService.crearPelicula(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // EDITAR
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(
            @PathVariable Long id,
            @RequestBody PeliculaRequest request) {

        peliculaService.actualizarPelicula(id, request);
        return ResponseEntity.noContent().build();
    }

    // ELIMINAR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        peliculaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
