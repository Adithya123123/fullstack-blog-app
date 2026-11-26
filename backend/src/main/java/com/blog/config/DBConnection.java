package com.blog.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity using standard JDBC.
 * Reads configuration from db.properties or system environment variables.
 */
public class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties properties = new Properties();

        // 1. Try to load from classpath resources (db.properties)
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not read db.properties from classpath: " + e.getMessage());
        }

        // 2. Read values with fallback to environment variables and default values
        url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/blog_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        }

        user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = properties.getProperty("db.user", "root");
        }

        password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = properties.getProperty("db.password", "root");
        }

        // 3. Register MySQL JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection from DriverManager.
     * Always remember to close this connection using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests whether the database connection is operational.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
