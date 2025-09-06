package unrn.model;

public class Formato {
    static final String ERROR_TIPO = "El formato no puede ser vacío";
    private final String tipo;

    public Formato(String tipo) {
        assertTipo(tipo);
        this.tipo = tipo;
    }

    private void assertTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new RuntimeException(ERROR_TIPO);
        }
    }

    public String tipo() {
        return tipo;
    }
}
