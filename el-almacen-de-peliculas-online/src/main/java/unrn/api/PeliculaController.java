package unrn.api;

import org.springframework.http.ResponseEntity;
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
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "genero", required = false) String genero,
            @RequestParam(name = "formato", required = false) String formato,
            @RequestParam(name = "condicion", required = false) String condicion,
            @RequestParam(name = "actor", required = false) String actor,
            @RequestParam(name = "director", required = false) String director,
            @RequestParam(name = "minPrecio", required = false) BigDecimal minPrecio,
            @RequestParam(name = "maxPrecio", required = false) BigDecimal maxPrecio,
            @RequestParam(name = "desde", required = false) String desde,
            @RequestParam(name = "hasta", required = false) String hasta,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            @RequestParam(name = "sort", defaultValue = "titulo") String sort,
            @RequestParam(name = "asc", defaultValue = "true") boolean asc) {
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        var pageResult = service.buscarPaginadoDetalle(
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
}
