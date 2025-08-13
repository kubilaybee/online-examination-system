package com.mobildev.exam.handlers;

import com.mobildev.exam.util.JwtUtil;
import com.mobildev.exam.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.io.IOException;
import java.util.logging.Logger;

public class JwtAuthHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(JwtAuthHandler.class.getName());
    private final HttpHandler nextHandler;

    public JwtAuthHandler(HttpHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.sendResponse(exchange, 401, "Authorization token is missing or invalid.");
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            Jws<Claims> claims = JwtUtil.parseToken(token);

            String username = claims.getBody().getSubject();
            exchange.setAttribute("username", username);

            nextHandler.handle(exchange);
        } catch (ExpiredJwtException e) {
            LOGGER.warning("Token has expired. " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 401, "Token has expired.");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            LOGGER.warning("Invalid token. " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 401, "Invalid token.");
        } catch (Exception e) {
            LOGGER.severe("Internal Server Error during token validation. " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 500, "Internal Server Error during token validation.");
        }
    }
}
