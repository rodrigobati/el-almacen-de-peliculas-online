package unrn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.dto.PeliculaRequest;
import unrn.event.movie.MovieEventEnvelope;
import unrn.event.movie.MovieEventPayload;
import unrn.event.movie.MovieEventPublisher;
import unrn.infra.persistence.ActorRepository;
import unrn.infra.persistence.DirectorRepository;
import unrn.infra.persistence.PageResult;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PeliculaService {

    static final String ERROR_DIRECTOR_INEXISTENTE = "Algún director no existe";
    static final String ERROR_ACTOR_INEXISTENTE = "Algún actor no existe";
    static final String ERROR_PELICULA_INEXISTENTE = "La película no existe";

    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final PeliculaRepository peliculaRepository;
    private final MovieEventPublisher eventPublisher;

    public PeliculaService(DirectorRepository directorRepository,
            ActorRepository actorRepository,
            PeliculaRepository peliculaRepository,
            MovieEventPublisher eventPublisher) {
        this.directorRepository = directorRepository;
        this.actorRepository = actorRepository;
        this.peliculaRepository = peliculaRepository;
        this.eventPublisher = eventPublisher;
    }

    // -------------------------
    // CREAR
    // -------------------------
    @Transactional
    public void crearPelicula(PeliculaRequest request) {
        var directores = obtenerDirectoresDesdeIds(request.directoresIds());
        var actores = obtenerActoresDesdeIds(request.actoresIds());

        var pelicula = new Pelicula(
                request.titulo(),
                new Condicion(request.condicion()),
                directores,
                request.precio(),
                new Formato(request.formato()),
                new Genero(request.genero()),
                request.sinopsis(),
                actores,
                request.imagenUrl(),
                request.fechaSalida(),
                request.rating());

        var guardada = peliculaRepository.guardar(pelicula);
        var payload = payloadDesde(guardada);
        eventPublisher.publishAfterCommit(MovieEventEnvelope.created(payload));
    }

    // -------------------------
    // EDITAR
    // -------------------------
    @Transactional
    public void actualizarPelicula(Long id, PeliculaRequest request) {
        var existente = peliculaRepository.porId(id);
        if (existente == null) {
            throw new RuntimeException(ERROR_PELICULA_INEXISTENTE);
        }

        var directores = obtenerDirectoresDesdeIds(request.directoresIds());
        var actores = obtenerActoresDesdeIds(request.actoresIds());

        var peliculaActualizada = new Pelicula(
                request.titulo(),
                new Condicion(request.condicion()),
                directores,
                request.precio(),
                new Formato(request.formato()),
                new Genero(request.genero()),
                request.sinopsis(),
                actores,
                request.imagenUrl(),
                request.fechaSalida(),
                request.rating());

        var actualizada = peliculaRepository.actualizar(id, peliculaActualizada);
        var payload = payloadDesde(actualizada);
        eventPublisher.publishAfterCommit(MovieEventEnvelope.updated(payload));
    }

    // -------------------------
    // ELIMINAR
    // -------------------------
    @Transactional
    public void eliminar(Long id) {
        var eliminada = peliculaRepository.eliminar(id);
        if (eliminada == null) {
            throw new RuntimeException(ERROR_PELICULA_INEXISTENTE);
        }

        var payload = payloadDesde(eliminada);
        eventPublisher.publishAfterCommit(MovieEventEnvelope.retired(payload));
    }

    // -------------------------
    // LISTAR
    // -------------------------
    @Transactional(readOnly = true)
    public List<Pelicula> listarTodas() {
        return peliculaRepository.listarTodos();
    }

    @Transactional(readOnly = true)
    public Page<Pelicula> buscarPaginado(
            String q,
            String genero,
            String formato,
            String condicion,
            String actor,
            String director,
            BigDecimal minPrecio,
            BigDecimal maxPrecio,
            LocalDate desde,
            LocalDate hasta,
            int page,
            int size,
            String sort,
            boolean asc) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? 12 : size;

        PageResult<Pelicula> result = peliculaRepository.buscarPaginado(
                q,
                genero,
                formato,
                condicion,
                actor,
                director,
                desde,
                hasta,
                minPrecio,
                maxPrecio,
                safePage,
                safeSize,
                sort,
                asc);

        String safeSort = (sort == null || sort.isBlank()) ? "titulo" : sort;
        Sort sortSpec = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, safeSort);
        Pageable pageable = PageRequest.of(result.getPage(), result.getSize(), sortSpec);
        return new PageImpl<>(result.getItems(), pageable, result.getTotal());
    }

    private MovieEventPayload payloadDesde(Pelicula pelicula) {
        return new MovieEventPayload(
                pelicula.id(),
                pelicula.titulo(),
                pelicula.precio(),
                pelicula.activa(),
                pelicula.version());
    }

    // -------------------------
    // HELPERS
    // -------------------------
    private List<Director> obtenerDirectoresDesdeIds(List<Long> directoresIds) {
        if (directoresIds == null || directoresIds.isEmpty()) {
            return List.of();
        }

        var directores = directorRepository.findAllById(directoresIds);

        if (directores.size() != directoresIds.size()) {
            throw new RuntimeException(ERROR_DIRECTOR_INEXISTENTE);
        }

        return List.copyOf(directores);
    }

    private List<Actor> obtenerActoresDesdeIds(List<Long> actoresIds) {
        if (actoresIds == null || actoresIds.isEmpty()) {
            return List.of();
        }

        var actores = actorRepository.findAllById(actoresIds);

        if (actores.size() != actoresIds.size()) {
            throw new RuntimeException(ERROR_ACTOR_INEXISTENTE);
        }

        return List.copyOf(actores);
    }
}
