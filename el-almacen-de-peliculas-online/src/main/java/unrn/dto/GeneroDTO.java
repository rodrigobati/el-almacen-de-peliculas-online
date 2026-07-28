package unrn.dto;

/**
 * DTO simple de genero para transportar categorias o clasificaciones visibles.
 *
 * Mantiene el contrato de salida liviano cuando el cliente solo necesita mostrar o
 * seleccionar el nombre del genero.
 */
public record GeneroDTO(
        String id,
        String name,
        String description
) {
}
