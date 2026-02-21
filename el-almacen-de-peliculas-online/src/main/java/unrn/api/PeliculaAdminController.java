package unrn.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unrn.dto.DetallePeliculaDTO;
import unrn.dto.PeliculaRequest;
import unrn.service.PeliculaService;

@RestController
@RequestMapping("/api/admin/peliculas")
public class PeliculaAdminController {

    private final PeliculaService peliculaService;

    public PeliculaAdminController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    // LISTAR
    @GetMapping
    public ResponseEntity<PageResponse<DetallePeliculaDTO>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) String condicion,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) java.math.BigDecimal minPrecio,
            @RequestParam(required = false) java.math.BigDecimal maxPrecio,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "titulo") String sort,
            @RequestParam(defaultValue = "true") boolean asc) {

        java.time.LocalDate d = (desde == null || desde.isBlank()) ? null : java.time.LocalDate.parse(desde);
        java.time.LocalDate h = (hasta == null || hasta.isBlank()) ? null : java.time.LocalDate.parse(hasta);

        var pageResult = peliculaService.buscarPaginadoDetalle(
                q,
                genero,
                formato,
                condicion,
                actor,
                director,
                minPrecio,
                maxPrecio,
                d,
                h,
                page,
                size,
                sort,
                asc);

        var response = PageResponse.of(
                pageResult.getContent(),
                pageResult.getTotalElements(),
                pageResult.getNumber(),
                pageResult.getSize());

        return ResponseEntity.ok(response);
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
