package unrn.app;

import unrn.infra.persistence.PeliculaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DemoConsultas {
    public static void main(String[] args) {
        var repo = new PeliculaRepository();

        System.out.println("¿Existe Blade Runner? " + repo.existePorTitulo("Blade Runner"));
        System.out.println("Total DRAMA: " + repo.contarPorGenero("DRAMA"));

        var pagina = repo.buscarPaginado("blade", 0, 10, "titulo", true);
        System.out.println("Página 0, total=" + pagina.getTotal() + ", items=" + pagina.getItems().size());

        var rangoPrecio = repo.buscarPorPrecioEntre(new BigDecimal("0"), new BigDecimal("20000"));
        System.out.println("Por precio <= 20k: " + rangoPrecio.size());

        var dinamico = repo.buscarDinamico("blade", null, null, null, LocalDate.of(1900, 1, 1), LocalDate.now(), null,
                null);
        System.out.println("Dinámico: " + dinamico.size());
    }
}
