package unrn.infra.persistence;

import java.util.List;

/**
 * Resultado paginado interno devuelto por repositorios de infraestructura.
 *
 * Transporta items, total, pagina y tamanio sin depender de Page de Spring Data,
 * permitiendo que la capa de servicio decida como exponer esa informacion hacia la
 * API.
 */
public class PageResult<T> {
    private final List<T> items;
    private final long total;
    private final int page;
    private final int size;

    /**
     * Inicializa una instancia de PageResult con los datos necesarios.
     */
    public PageResult(List<T> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    /**
     * Devuelve los elementos de la pagina actual.
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * Devuelve la cantidad total de elementos que cumplen la consulta.
     */
    public long getTotal() {
        return total;
    }

    /**
     * Devuelve el numero de pagina devuelto por la consulta.
     */
    public int getPage() {
        return page;
    }

    /**
     * Devuelve el tamanio de pagina usado para consultar.
     */
    public int getSize() {
        return size;
    }
}
