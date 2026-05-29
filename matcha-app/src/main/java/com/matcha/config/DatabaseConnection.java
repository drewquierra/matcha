package com.matcha.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — Thread-safe singleton managing the MySQL JDBC connection.
 * Configuration is loaded dynamically from a .env file via the dotenv-java library.
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String host     = dotenv.get("DB_HOST",     "localhost");
        String port     = dotenv.get("DB_PORT",     "3306");
        String dbName   = dotenv.get("DB_NAME",     "matcha_db");
        String user     = dotenv.get("DB_USER",     "root");
        String password = dotenv.get("DB_PASSWORD", "");

        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
            host, port, dbName
        );

        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Failed to establish database connection. " +
                "Check your .env configuration and ensure MySQL is running.\n" +
                "Cause: " + e.getMessage(), e
            );
        }
    }

    /**
     * Returns the singleton instance, creating it on first access (double-checked locking).
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the active JDBC connection, re-establishing it if it has been closed.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instance = null;
                return DatabaseConnection.getInstance().connection;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate database connection.", e);
        }
        return connection;
    }

    /**
     * Gracefully closes the active database connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Warning: Could not close database connection — " + e.getMessage());
        }
    }
}
