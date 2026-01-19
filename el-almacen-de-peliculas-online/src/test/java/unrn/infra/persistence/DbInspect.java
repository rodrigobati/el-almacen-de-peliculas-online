package unrn.infra.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbInspect {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://127.0.0.1:3306/almacen-peliculas-peliculas?useSSL=false&serverTimezone=UTC";
        String user = "mysqlusuario";
        String pass = "mequieroir";
        try (Connection c = DriverManager.getConnection(url, user, pass);
                Statement s = c.createStatement()) {
            System.out.println("Connected to " + url);
            try (ResultSet rs = s.executeQuery("SHOW COLUMNS FROM pelicula")) {
                System.out.printf("%-20s %-20s %-10s\n", "Field", "Type", "Null");
                while (rs.next()) {
                    System.out.printf("%-20s %-20s %-10s\n", rs.getString("Field"), rs.getString("Type"),
                            rs.getString("Null"));
                }
            }
        }
    }
}
