package com.blog.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for HTTP request parsing and response handling.
 * Manages JSON serialization with Gson, CORS headers, cookies, and HTTP status codes.
 */
public class ResponseUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    /**
     * Reads the entire HTTP request body as a String (UTF-8).
     */
    public static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Parses the request body JSON directly into a target Java class.
     */
    public static <T> T parseRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        String body = readRequestBody(exchange);
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        return gson.fromJson(body, clazz);
    }

    /**
     * Sets standard CORS headers to permit requests from local dev or cloud frontend.
     */
    public static void setCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();

        // 1. Check configured FRONTEND_URL env var
        String frontendEnv = System.getenv("FRONTEND_URL");
        String requestOrigin = exchange.getRequestHeaders().getFirst("Origin");

        if (requestOrigin != null && !requestOrigin.trim().isEmpty()) {
            headers.set("Access-Control-Allow-Origin", requestOrigin);
        } else if (frontendEnv != null && !frontendEnv.trim().isEmpty()) {
            headers.set("Access-Control-Allow-Origin", frontendEnv.trim());
        } else {
            headers.set("Access-Control-Allow-Origin", "http://localhost:5173");
        }

        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Cookie");
    }

    /**
     * Handles CORS preflight (OPTIONS) requests.
     * Returns true if request was OPTIONS and handled, false otherwise.
     */
    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            setCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    /**
     * Sends a JSON response with the given HTTP status code.
     */
    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        String jsonResponse = gson.toJson(data);
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
            os.flush();
        }
    }

    /**
     * Sends a structured error response: { "success": false, "message": "..." }
     */
    public static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("success", false);
        errorBody.put("message", message);
        sendJsonResponse(exchange, statusCode, errorBody);
    }

    /**
     * Sends a structured success response with a message and optional payload.
     */
    public static void sendSuccessResponse(HttpExchange exchange, int statusCode, String message, Object data) throws IOException {
        Map<String, Object> successBody = new HashMap<>();
        successBody.put("success", true);
        successBody.put("message", message);
        if (data != null) {
            successBody.put("data", data);
        }
        sendJsonResponse(exchange, statusCode, successBody);
    }

    /**
     * Extracts the sessionId cookie value from the request Cookie header.
     */
    public static String getSessionIdFromCookie(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return null;
        }

        // Split cookies separated by ';'
        String[] cookies = cookieHeader.split(";");
        for (String cookie : cookies) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && "sessionId".equalsIgnoreCase(parts[0].trim())) {
                return parts[1].trim();
            }
        }
        return null;
    }

    /**
     * Attaches an HttpOnly session cookie to the response.
     * Uses SameSite=None; Secure on HTTPS cloud deployments for cross-domain cookie handling.
     */
    public static void setSessionCookie(HttpExchange exchange, String sessionId) {
        String isSecure = System.getenv("COOKIE_SECURE");
        boolean secureMode = "true".equalsIgnoreCase(isSecure) || "production".equalsIgnoreCase(System.getenv("ENVIRONMENT"));

        String cookieValue;
        if (secureMode) {
            cookieValue = String.format("sessionId=%s; Path=/; HttpOnly; SameSite=None; Secure; Partitioned; Max-Age=86400", sessionId);
        } else {
            cookieValue = String.format("sessionId=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400", sessionId);
        }
        exchange.getResponseHeaders().add("Set-Cookie", cookieValue);
    }

    /**
     * Clears the session cookie by setting Max-Age=0.
     */
    public static void clearSessionCookie(HttpExchange exchange) {
        String isSecure = System.getenv("COOKIE_SECURE");
        boolean secureMode = "true".equalsIgnoreCase(isSecure) || "production".equalsIgnoreCase(System.getenv("ENVIRONMENT"));

        String cookieValue;
        if (secureMode) {
            cookieValue = "sessionId=; Path=/; HttpOnly; SameSite=None; Secure; Partitioned; Max-Age=0";
        } else {
            cookieValue = "sessionId=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0";
        }
        exchange.getResponseHeaders().add("Set-Cookie", cookieValue);
    }
}
