package unrn.service;

import org.springframework.stereotype.Service;
import unrn.model.*;
import unrn.dto.DetallePeliculaDTO;
import unrn.infra.persistence.PeliculaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PeliculaService {

    private final PeliculaRepository peliculaRepository;

    public PeliculaService(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    public Long crear(DetallePeliculaDTO dto) {
        var pelicula = new Pelicula(
                dto.titulo(),
                new Condicion(dto.condicion()),
                dto.directores().stream()
                        .map(Director::new)
                        .collect(Collectors.toList()),
                dto.precio(),
                new Formato(dto.formato()),
                new Genero(dto.genero()),
                dto.sinopsis(),
                dto.actores().stream()
                        .map(Actor::new)
                        .collect(Collectors.toList()),
                dto.imagenUrl(),
                dto.fechaSalida(),
                dto.rating());

        return peliculaRepository.guardar(pelicula);
    }

    public void editar(Long id, DetallePeliculaDTO dto) {
        var pelicula = peliculaRepository.porId(id);
        if (pelicula == null) {
            throw new RuntimeException("No existe la película con id " + id);
        }

        var peliculaActualizada = new Pelicula(
                dto.titulo(),
                new Condicion(dto.condicion()),
                dto.directores().stream()
                        .map(Director::new)
                        .collect(Collectors.toList()),
                dto.precio(),
                new Formato(dto.formato()),
                new Genero(dto.genero()),
                dto.sinopsis(),
                dto.actores().stream()
                        .map(Actor::new)
                        .collect(Collectors.toList()),
                dto.imagenUrl(),
                dto.fechaSalida(),
                dto.rating());

        pelicula.actualizarDesde(peliculaActualizada);
        peliculaRepository.guardar(pelicula);
    }

    public void eliminar(Long id) {
        var pelicula = peliculaRepository.porId(id);
        if (pelicula != null) {
            peliculaRepository.eliminar(id);
        }
    }

    public List<DetallePeliculaDTO> listar() {
        return peliculaRepository.listarTodos().stream()
                .map(DetallePeliculaDTO::from)
                .collect(Collectors.toList());
    }
}
