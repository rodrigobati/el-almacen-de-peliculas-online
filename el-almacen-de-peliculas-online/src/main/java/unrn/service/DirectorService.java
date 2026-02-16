package unrn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.dto.DirectorAdminDTO;
import unrn.infra.persistence.DirectorEntity;
import unrn.infra.persistence.DirectorRepository;

import java.util.List;

@Service
public class DirectorService {

    static final String ERROR_NOMBRE_REQUERIDO = "El nombre del director es obligatorio";
    static final String ERROR_NOMBRE_DUPLICADO = "Ya existe un director con ese nombre";
    static final int DEFAULT_PAGE = 0;
    static final int DEFAULT_SIZE = 15;

    private final DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    @Transactional(readOnly = true)
    public List<DirectorAdminDTO> buscar(String q, Integer page, Integer size) {
        int pageNormalizada = normalizePage(page);
        int sizeNormalizado = normalizeSize(size);

        return directorRepository.buscarPorNombre(q, pageNormalizada, sizeNormalizado)
                .stream()
                .map(DirectorAdminDTO::from)
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
    public DirectorAdminDTO crear(String nombre) {
        String nombreNormalizado = assertNombre(nombre);
        assertNoDuplicado(nombreNormalizado);

        DirectorEntity nuevo = new DirectorEntity(nombreNormalizado);
        DirectorEntity guardado = directorRepository.guardar(nuevo);
        return DirectorAdminDTO.from(guardado);
    }

    private String assertNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationRuntimeException(ERROR_NOMBRE_REQUERIDO);
        }
        return nombre.trim();
    }

    private void assertNoDuplicado(String nombre) {
        if (directorRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ValidationRuntimeException(ERROR_NOMBRE_DUPLICADO);
        }
    }
}
