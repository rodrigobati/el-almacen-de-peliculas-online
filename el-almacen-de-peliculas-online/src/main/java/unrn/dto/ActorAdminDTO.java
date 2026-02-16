package unrn.dto;

import unrn.infra.persistence.ActorEntity;

public record ActorAdminDTO(
        Long id,
        String nombre) {

    public static ActorAdminDTO from(ActorEntity actor) {
        return new ActorAdminDTO(actor.id(), actor.nombre());
    }
}
