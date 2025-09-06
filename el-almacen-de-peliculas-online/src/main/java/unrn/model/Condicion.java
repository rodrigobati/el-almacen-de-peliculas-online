package unrn.model;

public final class Condicion {
    static final String ERROR_CONDICION = "La condición debe ser 'nuevo' o 'usado'";
    private final String valor;

    public static final Condicion NUEVO = new Condicion("nuevo");
    public static final Condicion USADO = new Condicion("usado");

    public Condicion(String valor) {
        assertValor(valor);
        this.valor = valor.toLowerCase();
    }

    private void assertValor(String valor) {
        if (valor == null || (!valor.equalsIgnoreCase("nuevo") && !valor.equalsIgnoreCase("usado"))) {
            throw new RuntimeException(ERROR_CONDICION);
        }
    }

    public String valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Condicion that = (Condicion) o;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return valor;
    }
}
