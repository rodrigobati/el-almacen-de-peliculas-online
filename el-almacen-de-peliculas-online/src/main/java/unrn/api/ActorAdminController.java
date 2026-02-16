package unrn.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import unrn.dto.ActorAdminDTO;
import unrn.dto.NombreRequest;
import unrn.service.ActorService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/actores")
public class ActorAdminController {

    private final ActorService actorService;

    public ActorAdminController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    public ResponseEntity<List<ActorAdminDTO>> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        return ResponseEntity.ok(actorService.buscar(q, page, size));
    }

    @PostMapping
    public ResponseEntity<ActorAdminDTO> crear(@RequestBody NombreRequest request) {
        ActorAdminDTO creado = actorService.crear(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
