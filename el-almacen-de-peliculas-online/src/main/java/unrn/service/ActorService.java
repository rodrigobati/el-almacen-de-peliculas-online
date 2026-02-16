package unrn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.dto.ActorAdminDTO;
import unrn.infra.persistence.ActorEntity;
import unrn.infra.persistence.ActorRepository;

import java.util.List;

@Service
public class ActorService {

    static final String ERROR_NOMBRE_REQUERIDO = "El nombre del actor es obligatorio";
    static final String ERROR_NOMBRE_DUPLICADO = "Ya existe un actor con ese nombre";
    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_SIZE = 15;

    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    @Transactional(readOnly = true)
    public List<ActorAdminDTO> buscar(String q, Integer page, Integer size) {
        int pageNormalizada = normalizePage(page);
        int sizeNormalizado = normalizeSize(size);

        return actorRepository.buscarPorNombre(q, pageNormalizada, sizeNormalizado)
                .stream()
                .map(ActorAdminDTO::from)
                .toList();
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return size;
    }

    @Transactional
    public ActorAdminDTO crear(String nombre) {
        String nombreNormalizado = assertNombre(nombre);
        assertNoDuplicado(nombreNormalizado);

        ActorEntity nuevo = new ActorEntity(nombreNormalizado);
        ActorEntity guardado = actorRepository.guardar(nuevo);
        return ActorAdminDTO.from(guardado);
    }

    private String assertNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationRuntimeException(ERROR_NOMBRE_REQUERIDO);
        }
        return nombre.trim();
    }

    private void assertNoDuplicado(String nombre) {
        if (actorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ValidationRuntimeException(ERROR_NOMBRE_DUPLICADO);
        }
    }
}
