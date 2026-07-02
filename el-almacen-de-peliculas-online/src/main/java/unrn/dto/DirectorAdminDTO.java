package unrn.dto;

import unrn.infra.persistence.DirectorEntity;

/**
 * DTO de salida para directores vistos desde la administracion.
 *
 * Lleva id y nombre para que el backoffice pueda buscar directores existentes y
 * asociarlos a peliculas sin exponer la entidad de base de datos.
 */
public record DirectorAdminDTO(
        Long id,
        String nombre) {

    /**
     * Construye un DTO a partir del objeto de origen recibido.
     */
    public static DirectorAdminDTO from(DirectorEntity director) {
        return new DirectorAdminDTO(director.id(), director.nombre());
    }
}
