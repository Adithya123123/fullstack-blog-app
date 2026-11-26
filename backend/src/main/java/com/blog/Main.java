package com.blog;

import com.blog.config.DBConnection;
import com.blog.handler.AuthHandler;
import com.blog.handler.PostHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application entry point.
 * Initializes the HTTP server, tests database connectivity, registers route handlers,
 * and starts listening for requests.
 */
public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("    FULL-STACK BLOG APPLICATION - BACKEND SERVER    ");
        System.out.println("====================================================");

        // 1. Verify Database Connectivity
        System.out.println("[1/3] Testing database connection...");
        if (DBConnection.testConnection()) {
            System.out.println("      MySQL Database connection successful!");
        } else {
            System.err.println("      WARNING: Failed to connect to MySQL database.");
            System.err.println("      Please ensure MySQL service is running and credentials in db.properties are correct.");
        }

        // 2. Determine Port
        int port = DEFAULT_PORT;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portEnv.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        // 3. Initialize Java HttpServer
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Handler instances
            AuthHandler authHandler = new AuthHandler();
            PostHandler postHandler = new PostHandler();

            // Register Authentication routes
            server.createContext("/api/register", authHandler);
            server.createContext("/api/login", authHandler);
            server.createContext("/api/logout", authHandler);
            server.createContext("/api/me", authHandler);

            // Register Blog Post routes
            server.createContext("/api/posts", postHandler);
            server.createContext("/api/my-posts", postHandler);

            // Use a multi-threaded executor to handle multiple concurrent requests
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Start server
            server.start();

            System.out.println("[2/3] Registered API Endpoints:");
            System.out.println("      POST   /api/register");
            System.out.println("      POST   /api/login");
            System.out.println("      POST   /api/logout");
            System.out.println("      GET    /api/me");
            System.out.println("      GET    /api/posts");
            System.out.println("      GET    /api/posts/{id}");
            System.out.println("      POST   /api/posts");
            System.out.println("      PUT    /api/posts/{id}");
            System.out.println("      DELETE /api/posts/{id}");
            System.out.println("      GET    /api/my-posts");
            System.out.println("      GET    /api/posts?search={keyword}");
            System.out.println("[3/3] Backend server started successfully on port " + port);
            System.out.println("      Base URL: http://localhost:" + port);
            System.out.println("====================================================");

            // Graceful shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down backend server...");
                server.stop(1);
                System.out.println("Server stopped.");
            }));

        } catch (IOException e) {
            System.err.println("Fatal: Could not start HttpServer on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
