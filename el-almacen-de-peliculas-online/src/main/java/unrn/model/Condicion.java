package unrn.model;

/**
 * Representa la condicion comercial de una pelicula disponible en el catalogo.
 *
 * Centraliza valores como nuevo y usado, valida que la condicion no sea vacia y
 * compara por valor para que la misma condicion sea reconocida aunque se cree en
 * distintos puntos de la aplicacion.
 */
public final class Condicion {
    static final String ERROR_CONDICION = "La condiciÃ³n debe ser 'nuevo' o 'usado'";
    private final String valor;

    public static final Condicion NUEVO = new Condicion("nuevo");
    public static final Condicion USADO = new Condicion("usado");

    /**
     * Inicializa una instancia de Condicion con los datos necesarios.
     */
    public Condicion(String valor) {
        assertValor(valor);
        this.valor = valor.toLowerCase();
    }

    /**
     * Exige que la condicion sea nuevo o usado.
     */
    private void assertValor(String valor) {
        if (valor == null || (!valor.equalsIgnoreCase("nuevo") && !valor.equalsIgnoreCase("usado"))) {
            throw new RuntimeException(ERROR_CONDICION);
        }
    }

    /**
     * Devuelve el valor de valor.
     */
    public String valor() {
        return valor;
    }

    /**
     * Compara este objeto con otro segun su identidad de valor.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condicion that = (Condicion) o;
        return valor.equals(that.valor);
    }

    /**
     * Calcula el hash consistente con equals.
     */
    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    /**
     * Devuelve la representacion textual del objeto.
     */
    @Override
    public String toString() {
        return valor;
    }
}
