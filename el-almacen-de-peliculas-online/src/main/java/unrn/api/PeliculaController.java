package unrn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unrn.dto.DetallePeliculaDTO;
import unrn.infra.persistence.PeliculaRepository;
import unrn.service.PeliculaService;
import unrn.model.Pelicula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/peliculas")
public class PeliculaController {

    private final PeliculaRepository repo;
    private final PeliculaService service;

    public PeliculaController(PeliculaRepository repo, PeliculaService service) {
        this.repo = repo;
        this.service = service;
    }

    // GET /api/peliculas/{id} -> detalle para la vista de React
    @GetMapping("/{id}")
    public ResponseEntity<DetallePeliculaDTO> detalle(@PathVariable Long id) {
        Pelicula p = repo.porId(id);
        if (p == null)
            throw new NotFound("Película no encontrada: id=" + id);
        return ResponseEntity.ok(DetallePeliculaDTO.from(p));
    }

    // GET
    // /api/peliculas?q=blade&genero=DRAMA&actor=Ford&director=Scott&minPrecio=0&maxPrecio=10000&desde=1980-01-01&hasta=1990-12-31&page=0&size=12&sort=titulo&asc=true
    @GetMapping
    public ResponseEntity<PageResponse<DetallePeliculaDTO>> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) String condicion,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String director,
            @RequestParam(required = false) BigDecimal minPrecio,
            @RequestParam(required = false) BigDecimal maxPrecio,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "titulo") String sort,
            @RequestParam(defaultValue = "true") boolean asc) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        Page<Pelicula> pageResult = service.buscarPaginado(
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

        var dtoItems = pageResult.getContent().stream().map(DetallePeliculaDTO::from).toList();
        var response = new PageResponse<>(
                dtoItems,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getNumber(),
                pageResult.getSize());

        return ResponseEntity.ok(response);
    }
}
