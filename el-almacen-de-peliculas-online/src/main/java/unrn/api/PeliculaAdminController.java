package unrn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import unrn.dto.DetallePeliculaDTO;
import unrn.service.PeliculaService;

@RestController
@RequestMapping("/api/admin/peliculas")
@PreAuthorize("hasRole('ADMIN')")
public class PeliculaAdminController {

    private final PeliculaService service;

    public PeliculaAdminController(PeliculaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody DetallePeliculaDTO dto) {
        var id = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/peliculas/" + id)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editar(@PathVariable Long id, @RequestBody DetallePeliculaDTO dto) {
        try {
            service.editar(id, dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
