package unrn.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.dto.PeliculaRequest;
import unrn.infra.persistence.ActorRepository;
import unrn.infra.persistence.DirectorRepository;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.*;

import java.util.List;

@Service
public class PeliculaService {

    static final String ERROR_DIRECTOR_INEXISTENTE = "Algún director no existe";
    static final String ERROR_ACTOR_INEXISTENTE = "Algún actor no existe";

    private final DirectorRepository directorRepository;
    private final ActorRepository actorRepository;
    private final PeliculaRepository peliculaRepository;

    public PeliculaService(DirectorRepository directorRepository,
            ActorRepository actorRepository,
            PeliculaRepository peliculaRepository) {
        this.directorRepository = directorRepository;
        this.actorRepository = actorRepository;
        this.peliculaRepository = peliculaRepository;
    }

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

        // 🔴 ANTES (no existe en tu repo)
        // peliculaRepository.save(pelicula);

        // ✅ AHORA: usar tu método real
        peliculaRepository.guardar(pelicula);
        // si querés usar el id:
        // Long id = peliculaRepository.guardar(pelicula);
    }

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
