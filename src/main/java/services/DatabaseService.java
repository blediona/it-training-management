package services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Set;

public class DatabaseService {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/it_training_center_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String URL = getConfig("db.url", "DB_URL", DEFAULT_URL);
    private static final String USER = getConfig("db.user", "DB_USER", "root");
    private static final String PASSWORD = getConfig("db.password", "DB_PASSWORD", "1234");

    private static final Set<String> REQUIRED_TABLES = Set.of(
            "roles",
            "permissions",
            "role_permissions",
            "users",
            "trainings",
            "candidates",
            "payments",
            "certificates",
            "password_reset_tokens",
            "audit_log"
    );

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void initializeDatabase() {
        try {
            validateConnection();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void validateConnection() throws Exception {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                throw new IllegalStateException("MySQL connection failed.");
            }
            DatabaseMetaData metaData = connection.getMetaData();
            for (String table : REQUIRED_TABLES) {
                try (ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, table, new String[]{"TABLE"})) {
                    if (!resultSet.next()) {
                        throw new IllegalStateException("Missing MySQL table: " + table);
                    }
                }
            }
        }
    }

    private static String getConfig(String propertyKey, String environmentKey, String defaultValue) {
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        return defaultValue;
    }
}
