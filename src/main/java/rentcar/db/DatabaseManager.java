package rentcar.db;

import rentcar.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DatabaseManager.class
                .getResourceAsStream("/rentcar/database.properties")) {
            if (in == null) throw new BusinessException("Не найден database.properties");
            PROPS.load(in);
            Class.forName("org.postgresql.Driver");
        } catch (IOException | ClassNotFoundException e) {
            throw new BusinessException("Не удалось загрузить настройки БД: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
    }
}