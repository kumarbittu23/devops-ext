package com.example.registration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class StudentRegistrationServer {
    private StudentRegistrationServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", StudentRegistrationServer::serveForm);
        server.createContext("/register", StudentRegistrationServer::register);
        server.start();
        System.out.println("Student Registration is listening on port " + port);
    }

    private static void serveForm(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod()) || !"/".equals(exchange.getRequestURI().getPath())) {
            respond(exchange, 404, "Not found");
            return;
        }

        try (var input = StudentRegistrationServer.class.getResourceAsStream("/static/index.html")) {
            if (input == null) {
                respond(exchange, 500, "Registration form is unavailable");
                return;
            }
            byte[] content = input.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.sendResponseHeaders(200, content.length);
            exchange.getResponseBody().write(content);
        } finally {
            exchange.close();
        }
    }

    private static void register(HttpExchange exchange) throws IOException {
        if (!"/register".equals(exchange.getRequestURI().getPath())) {
            respond(exchange, 404, "Not found");
            return;
        }
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Allow", "POST");
            respond(exchange, 405, "Method not allowed");
            return;
        }

        String body;
        try (var input = exchange.getRequestBody()) {
            body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Map<String, String> fields = parseForm(body);
        boolean missingFields = fields.getOrDefault("studentName", "").isBlank()
                || fields.getOrDefault("rollNumber", "").isBlank()
                || fields.getOrDefault("email", "").isBlank()
                || fields.getOrDefault("course", "").isBlank();
        String email = fields.getOrDefault("email", "");
        if (missingFields || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            respond(exchange, 400, "Enter all fields and a valid email address.");
            return;
        }

        respond(exchange, 200, "Registration submitted successfully.");
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> fields = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            fields.put(key, value);
        }
        return fields;
    }

    private static void respond(HttpExchange exchange, int status, String message) throws IOException {
        byte[] content = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, content.length);
        exchange.getResponseBody().write(content);
        exchange.close();
    }
}
