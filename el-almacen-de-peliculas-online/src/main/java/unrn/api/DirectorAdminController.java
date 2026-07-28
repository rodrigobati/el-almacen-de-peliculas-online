package unrn.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unrn.dto.DirectorAdminDTO;
import unrn.dto.NombreRequest;
import unrn.service.DirectorService;

import java.util.List;

/**
 * Controlador REST de backoffice para directores.
 *
 * Permite al administrador buscar directores existentes y registrar nuevos nombres
 * antes de asociarlos a peliculas. Mantiene al controlador como borde HTTP y deja
 * validacion, duplicados y persistencia en DirectorService.
 */
@RestController
@RequestMapping("/api/admin/directores")
public class DirectorAdminController {

    private final DirectorService directorService;

    /**
     * Inicializa una instancia de DirectorAdminController con los datos necesarios.
     */
    public DirectorAdminController(DirectorService directorService) {
        this.directorService = directorService;
    }

    /**
     * Busca directores para administracion por nombre y paginacion opcional.
     */
    @GetMapping
    public ResponseEntity<List<DirectorAdminDTO>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        return ResponseEntity.ok(directorService.buscar(q, page, size));
    }

    /**
     * Crea un director desde la API administrativa.
     */
    @PostMapping
    public ResponseEntity<DirectorAdminDTO> crear(@RequestBody NombreRequest request) {
        DirectorAdminDTO creado = directorService.crear(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
