package unrn.app;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;

/**
 * Runner manual para ejecutar ajustes de esquema bajo el perfil migrate.
 *
 * Se usa como herramienta operacional controlada cuando hay que aplicar cambios de
 * base que no forman parte del flujo normal de la aplicacion. No debe confundirse
 * con la logica de negocio ni con los repositorios del catalogo.
 */
@Component
@Profile("migrate")
public class SchemaMigrator implements CommandLineRunner {

    private final DataSource ds;

    /**
     * Inicializa una instancia de SchemaMigrator con los datos necesarios.
     */
    public SchemaMigrator(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Ejecuta la tarea inicial cuando Spring invoca este componente.
     */
    @Override
    public void run(String... args) throws Exception {
        String backupName = "pelicula_backup_" + Instant.now().getEpochSecond();
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            System.out.println("SchemaMigrator: creating backup table " + backupName);
            s.executeUpdate("CREATE TABLE IF NOT EXISTS " + backupName + " LIKE pelicula");
            s.executeUpdate("INSERT INTO " + backupName + " SELECT * FROM pelicula");

            System.out.println("SchemaMigrator: dropping old columns condicion, formato, genero from pelicula");
            s.executeUpdate("ALTER TABLE pelicula DROP COLUMN condicion, DROP COLUMN formato, DROP COLUMN genero");

            System.out.println("SchemaMigrator: migration completed successfully.");
        } catch (Exception e) {
            System.out.println("SchemaMigrator: migration failed: " + e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }
}
