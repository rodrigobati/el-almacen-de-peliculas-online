package unrn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.dto.AdminPeliculaDTO;
import unrn.dto.DetallePeliculaDTO;
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
import java.util.Map;
import java.util.Set;

/**
 * Servicio de aplicacion que orquesta los casos de uso principales de peliculas.
 *
 * Crea, actualiza, retira, lista y busca peliculas combinando validaciones de
 * entrada, resolucion de actores/directores, persistencia y publicacion de eventos
 * de catalogo. Es la capa que traduce pedidos de API en cambios de negocio.
 */
@Service
public class PeliculaService {

    static final String ERROR_DIRECTOR_INEXISTENTE = "AlgÃºn director no existe";
    static final String ERROR_ACTOR_INEXISTENTE = "AlgÃºn actor no existe";
    static final String ERROR_PELICULA_INEXISTENTE = "La pelÃ­cula no existe";
    static final int MAX_SIZE = 100;
    static final Set<String> SORT_ALLOWLIST = Set.of("titulo", "precio", "fechaSalida", "genero", "formato",
            "condicion");
    static final String ERROR_PAGE_INVALIDA = "El parÃ¡metro 'page' debe ser mayor o igual a 0";
    static final String ERROR_SIZE_INVALIDA = "El parÃ¡metro 'size' debe estar entre 1 y " + MAX_SIZE;
    static final String ERROR_SORT_INVALIDO = "El parÃ¡metro 'sort' no es vÃ¡lido";
    static final String ERROR_STOCK_REQUERIDO = "El stock es requerido";
    static final String ERROR_STOCK_INVALIDO = "El stock debe ser un numero entero mayor o igual a cero";
    static final String ERROR_VERSION_REQUERIDA = "La version es requerida";
    static final String ERROR_VERSION_INVALIDA = "La version debe ser mayor o igual a cero";

    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final PeliculaRepository peliculaRepository;
    private final MovieEventPublisher eventPublisher;

    /**
     * Inicializa una instancia de PeliculaService con los datos necesarios.
     */
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
    /**
     * Crea una pelicula, la persiste y publica el evento correspondiente.
     */
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
    /**
     * Actualiza una pelicula existente y publica el evento correspondiente.
     */
    @Transactional
    public void actualizarPelicula(Long id, PeliculaRequest request) {
        var existente = peliculaRepository.porId(id);
        if (existente == null) {
            throw new PeliculaNotFoundException(ERROR_PELICULA_INEXISTENTE);
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

    /**
     * Actualiza el stock disponible validando la version esperada.
     */
    @Transactional
    public AdminPeliculaDTO actualizarStock(Long id, BigDecimal stockDisponible, Long version) {
        BigDecimal stockNormalizado = normalizarStock(stockDisponible);
        long expectedVersion = normalizarVersion(version);

        var actualizada = peliculaRepository.actualizarStock(id, stockNormalizado, expectedVersion);
        if (actualizada == null) {
            throw new PeliculaNotFoundException(ERROR_PELICULA_INEXISTENTE);
        }

        return AdminPeliculaDTO.from(actualizada);
    }

    // -------------------------
    // ELIMINAR
    // -------------------------
    /**
     * Retira una pelicula existente y publica el evento de retiro.
     */
    @Transactional
    public void eliminar(Long id) {
        var eliminada = peliculaRepository.eliminar(id);
        if (eliminada == null) {
            throw new PeliculaNotFoundException(ERROR_PELICULA_INEXISTENTE);
        }

        var payload = payloadDesde(eliminada);
        eventPublisher.publishAfterCommit(MovieEventEnvelope.retired(payload));
    }

    // -------------------------
    // LISTAR
    // -------------------------
    /**
     * Lista todas las peliculas activas disponibles en catalogo.
     */
    @Transactional(readOnly = true)
    public List<Pelicula> listarTodas() {
        return peliculaRepository.listarTodos();
    }

    /**
     * Busca peliculas con filtros, orden y paginacion validada.
     */
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
        String safeSort = (sort == null) ? "titulo" : sort.trim();
        assertParametrosPaginacionYOrdenValidos(page, size, safeSort);

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
                page,
                size,
                safeSort,
                asc);

        Sort sortSpec = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, safeSort);
        Pageable pageable = PageRequest.of(result.getPage(), result.getSize(), sortSpec);
        return new PageImpl<>(result.getItems(), pageable, result.getTotal());
    }

    /**
     * Busca peliculas y las transforma al DTO publico de detalle.
     */
    @Transactional(readOnly = true)
    public Page<DetallePeliculaDTO> buscarPaginadoDetalle(
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
        return buscarPaginado(q, genero, formato, condicion, actor, director, minPrecio, maxPrecio, desde, hasta,
                page, size, sort, asc).map(DetallePeliculaDTO::from);
    }

    /**
     * Busca peliculas y las transforma al DTO administrativo.
     */
    @Transactional(readOnly = true)
    public Page<AdminPeliculaDTO> buscarPaginadoDetalleAdmin(
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
        return buscarPaginado(q, genero, formato, condicion, actor, director, minPrecio, maxPrecio, desde, hasta,
                page, size, sort, asc).map(AdminPeliculaDTO::from);
    }

    /**
     * Arma el payload de evento a partir de una pelicula de dominio.
     */
    private MovieEventPayload payloadDesde(Pelicula pelicula) {
        return new MovieEventPayload(
                pelicula.id(),
                pelicula.titulo(),
                pelicula.precio(),
                pelicula.activa(),
                pelicula.version());
    }

    /**
     * Valida pagina, tamanio y campo de ordenamiento permitidos.
     */
    private void assertParametrosPaginacionYOrdenValidos(int page, int size, String sort) {
        if (page < 0) {
            throw new CatalogoQueryValidationException(
                    "INVALID_PAGE",
                    ERROR_PAGE_INVALIDA,
                    Map.of("field", "page", "value", page, "rule", ">= 0"));
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new CatalogoQueryValidationException(
                    "INVALID_SIZE",
                    ERROR_SIZE_INVALIDA,
                    Map.of("field", "size", "value", size, "rule", "1.." + MAX_SIZE));
        }
        if (!SORT_ALLOWLIST.contains(sort)) {
            throw new CatalogoQueryValidationException(
                    "INVALID_SORT",
                    ERROR_SORT_INVALIDO,
                    Map.of("field", "sort", "value", sort, "allowed", SORT_ALLOWLIST));
        }
    }

    /**
     * Valida y normaliza el stock recibido desde la API admin.
     */
    private BigDecimal normalizarStock(BigDecimal stockDisponible) {
        if (stockDisponible == null) {
            throw new CatalogoQueryValidationException(
                    "STOCK_REQUIRED",
                    ERROR_STOCK_REQUERIDO,
                    Map.of("field", "stockDisponible", "rule", "required"));
        }
        if (stockDisponible.compareTo(BigDecimal.ZERO) < 0 || stockDisponible.stripTrailingZeros().scale() > 0) {
            throw new CatalogoQueryValidationException(
                    "INVALID_STOCK",
                    ERROR_STOCK_INVALIDO,
                    Map.of("field", "stockDisponible", "value", stockDisponible, "rule", "integer >= 0"));
        }
        return stockDisponible.setScale(2);
    }

    /**
     * Valida la version esperada recibida desde la API admin.
     */
    private long normalizarVersion(Long version) {
        if (version == null) {
            throw new CatalogoQueryValidationException(
                    "VERSION_REQUIRED",
                    ERROR_VERSION_REQUERIDA,
                    Map.of("field", "version", "rule", "required"));
        }
        if (version < 0) {
            throw new CatalogoQueryValidationException(
                    "INVALID_VERSION",
                    ERROR_VERSION_INVALIDA,
                    Map.of("field", "version", "value", version, "rule", ">= 0"));
        }
        return version;
    }

    /**
     * -------------------------
     * HELPERS
     * -------------------------
     * Resuelve directores de dominio a partir de sus identificadores.
     */
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

    /**
     * Resuelve actores de dominio a partir de sus identificadores.
     */
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
