package unrn.dto;

import unrn.infra.persistence.ActorEntity;

/**
 * DTO de salida para actores vistos desde la administracion.
 *
 * Transporta el identificador persistido y el nombre, que son los datos necesarios
 * para buscar, seleccionar o confirmar actores al cargar peliculas desde backoffice.
 */
public record ActorAdminDTO(
        Long id,
        String nombre) {

    /**
     * Construye un DTO a partir del objeto de origen recibido.
     */
    public static ActorAdminDTO from(ActorEntity actor) {
        return new ActorAdminDTO(actor.id(), actor.nombre());
    }
}
