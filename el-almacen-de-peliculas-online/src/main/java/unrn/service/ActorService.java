package unrn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.dto.ActorAdminDTO;
import unrn.infra.persistence.ActorEntity;
import unrn.infra.persistence.ActorRepository;

import java.util.List;

/**
 * Servicio de aplicacion para la administracion de actores.
 *
 * Coordina busquedas paginadas y altas de actores desde la API admin, validando
 * nombres de negocio y evitando duplicados antes de delegar la persistencia al
 * repositorio. Devuelve DTOs administrativos para no exponer entidades JPA.
 */
@Service
public class ActorService {

    static final String ERROR_NOMBRE_REQUERIDO = "El nombre del actor es obligatorio";
    static final String ERROR_NOMBRE_DUPLICADO = "Ya existe un actor con ese nombre";
    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_SIZE = 15;

    private final ActorRepository actorRepository;

    /**
     * Inicializa una instancia de ActorService con los datos necesarios.
     */
    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    /**
     * Busca actores por nombre para autocompletado o seleccion administrativa.
     */
    @Transactional(readOnly = true)
    public List<ActorAdminDTO> buscar(String q, Integer page, Integer size) {
        int pageNormalizada = normalizePage(page);
        int sizeNormalizado = normalizeSize(size);

        return actorRepository.buscarPorNombre(q, pageNormalizada, sizeNormalizado)
                .stream()
                .map(ActorAdminDTO::from)
                .toList();
    }

    /**
     * Normaliza el valor recibido antes de usarlo.
     */
    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    /**
     * Normaliza el valor recibido antes de usarlo.
     */
    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return size;
    }

    /**
     * Crea un actor administrable luego de validar nombre y duplicados.
     */
    @Transactional
    public ActorAdminDTO crear(String nombre) {
        String nombreNormalizado = assertNombre(nombre);
        assertNoDuplicado(nombreNormalizado);

        ActorEntity nuevo = new ActorEntity(nombreNormalizado);
        ActorEntity guardado = actorRepository.guardar(nuevo);
        return ActorAdminDTO.from(guardado);
    }

    /**
     * Exige que el nombre del actor administrado no sea nulo ni vacio.
     */
    private String assertNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationRuntimeException(ERROR_NOMBRE_REQUERIDO);
        }
        return nombre.trim();
    }

    /**
     * Impide crear actores duplicados ignorando mayusculas y minusculas.
     */
    private void assertNoDuplicado(String nombre) {
        if (actorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ValidationRuntimeException(ERROR_NOMBRE_DUPLICADO);
        }
    }
}
