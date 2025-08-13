package com.mobildev.exam.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobildev.exam.db.DatabaseManager;
import com.mobildev.exam.model.User;
import com.mobildev.exam.util.JwtUtil;
import com.mobildev.exam.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class LoginHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(LoginHandler.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // only accept post requests
        if (!"POST".equals(exchange.getRequestMethod())) {
            ResponseUtil.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                ResponseUtil.sendResponse(exchange, 401, "Authorization required");
                return;
            }

            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);
            String username = parts[0];
            String password = parts[1];

            User user = authenticate(username, password);

            if (user != null) {
                // success login
                // create JWT token
                String token = JwtUtil.generateToken(user.getUsername());

                // create JSON
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "Login successful!");
                responseMap.put("token", token);

                String response = objectMapper.writeValueAsString(responseMap);

                ResponseUtil.sendResponse(exchange, 200, response);
            } else {
                ResponseUtil.sendResponse(exchange, 401, "Invalid credentials");
            }
        } catch (Exception e) {
            LOGGER.severe("Login handler hatası: " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT id, username FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    return user;
                }
            }
        }
        return null;
    }
}
