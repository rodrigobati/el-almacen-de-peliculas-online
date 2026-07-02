package unrn.api;

import java.util.List;

/**
 * Envoltorio de respuesta para listados paginados expuestos por la API.
 *
 * Unifica la forma en que frontend recibe items, total de resultados, cantidad de
 * paginas y pagina actual, independientemente de si el listado es publico o admin.
 */
public record PageResponse<T>(List<T> items, long total, int totalPages, int page, int size) {
    /**
     * Construye una respuesta paginada con totales calculados.
     */
    public static <T> PageResponse<T> of(List<T> items, long total, int page, int size) {
        int safeSize = size <= 0 ? 1 : size;
        int safePage = Math.max(0, page);
        int computedTotalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new PageResponse<>(items, total, computedTotalPages, safePage, safeSize);
    }
}
