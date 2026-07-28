package unrn.dto;

/**
 * Request reutilizable para altas administrativas basadas solo en nombre.
 *
 * Lo usan endpoints como actores y directores, donde el dato de entrada relevante
 * es el nombre que luego sera validado contra reglas de obligatoriedad y duplicado.
 */
public record NombreRequest(String nombre) {
}
