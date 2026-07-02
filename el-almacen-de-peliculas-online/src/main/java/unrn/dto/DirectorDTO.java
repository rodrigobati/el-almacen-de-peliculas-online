package unrn.dto;

/**
 * DTO simple de director para respuestas publicas del catalogo.
 *
 * Se usa cuando la vista de pelicula necesita mostrar el nombre de direccion sin
 * detalles administrativos ni acoplamiento al modelo de dominio.
 */
public record DirectorDTO(
        String id,
        String name) {
}
