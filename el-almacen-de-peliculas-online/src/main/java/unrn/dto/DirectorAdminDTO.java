package unrn.dto;

import unrn.infra.persistence.DirectorEntity;

public record DirectorAdminDTO(
        Long id,
        String nombre) {

    public static DirectorAdminDTO from(DirectorEntity director) {
        return new DirectorAdminDTO(director.id(), director.nombre());
    }
}
