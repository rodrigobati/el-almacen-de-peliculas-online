package unrn.app;

import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@Profile("inspect")
public class SchemaInspector implements CommandLineRunner {

    private final DataSource ds;

    public SchemaInspector(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            System.out.println("SchemaInspector: connected, printing columns of pelicula:");
            try (ResultSet rs = s.executeQuery("SHOW COLUMNS FROM pelicula")) {
                System.out.printf("%-20s %-30s %-6s %-6s %-20s %s\n", "Field", "Type", "Null", "Key", "Default",
                        "Extra");
                while (rs.next()) {
                    System.out.printf("%-20s %-30s %-6s %-6s %-20s %s\n",
                            rs.getString("Field"), rs.getString("Type"), rs.getString("Null"), rs.getString("Key"),
                            rs.getString("Default"), rs.getString("Extra"));
                }
            }
        } catch (Exception e) {
            System.out.println("SchemaInspector: error while inspecting schema: " + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            // Stop the JVM after printing so the app doesn't continue running
            System.out.println("SchemaInspector: exiting JVM after inspection.");
            System.exit(0);
        }
    }
}
