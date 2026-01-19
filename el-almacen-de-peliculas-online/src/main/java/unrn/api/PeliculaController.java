package unrn.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unrn.dto.DetallePeliculaDTO;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.Pelicula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final PeliculaRepository repo;

    public PeliculaController(PeliculaRepository repo) {
        this.repo = repo;
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
        // Si hay filtros específicos de actor/director, usar named queries
        if (actor != null && !actor.isBlank()) {
            var list = repo.buscarPorActor(actor);
            return ResponseEntity.ok(paginar(list, page, size));
        }
        if (director != null && !director.isBlank()) {
            var list = repo.buscarPorDirector(director);
            return ResponseEntity.ok(paginar(list, page, size));
        }
        if (genero != null && !genero.isBlank() && (q == null || q.isBlank())) {
            var list = repo.buscarPorGenero(genero);
            return ResponseEntity.ok(paginar(list, page, size));
        }
        if (q != null && !q.isBlank() && genero == null && actor == null && director == null) {
            var list = repo.buscarPorTitulo(q);
            return ResponseEntity.ok(paginar(list, page, size));
        }

        // Rango de fechas y precios
        LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
        LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

        var filtradas = repo.buscarDinamico(q, genero, formato, condicion, d, h, minPrecio, maxPrecio);
        // Ordenamiento básico por título (ya está en Criteria por defecto)
        // Para ordenar por otros campos habría que ampliar Criteria

        // Paginado manual
        int total = filtradas.size();
        int from = Math.max(0, page * size);
        int to = Math.min(total, from + size);
        var pageItems = (from < to) ? filtradas.subList(from, to) : List.<Pelicula>of();

        var dtoItems = pageItems.stream().map(DetallePeliculaDTO::from).toList();
        return ResponseEntity.ok(new PageResponse<>(dtoItems, total, page, size));
    }

    private PageResponse<DetallePeliculaDTO> paginar(List<Pelicula> source, int page, int size) {
        int total = source.size();
        int from = Math.max(0, page * size);
        int to = Math.min(total, from + size);
        var pageItems = (from < to) ? source.subList(from, to) : List.<Pelicula>of();
        var dtoItems = pageItems.stream().map(DetallePeliculaDTO::from).toList();
        return new PageResponse<>(dtoItems, total, page, size);
    }
}
