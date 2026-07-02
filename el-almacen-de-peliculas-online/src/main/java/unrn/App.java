package unrn;

/**
 * Punto de entrada heredado del esqueleto Maven original.
 *
 * No es el arranque real de Spring Boot: la aplicacion productiva se inicia desde
 * unrn.app.Application. Esta clase queda como utilidad minima de consola y como
 * senial de codigo legado que no participa del flujo web del catalogo.
 */
public class App 
{
    /**
     * Punto de entrada para ejecutar esta aplicacion o utilidad.
     */
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
