package unrn.dto;

/**
 * DTO simple de actor para respuestas de lectura del catalogo.
 *
 * Se usa cuando el cliente solo necesita mostrar informacion del reparto sin datos
 * administrativos ni detalles de persistencia.
 */
public record ActorDTO(
        String id,
        String name) {
}
