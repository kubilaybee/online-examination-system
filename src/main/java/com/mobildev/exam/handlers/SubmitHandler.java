package com.mobildev.exam.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobildev.exam.dto.ExamSubmissionDTO;
import com.mobildev.exam.service.SubmitService;
import com.mobildev.exam.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SubmitHandler implements HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(SubmitHandler.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SubmitService submitService;

    public SubmitHandler(SubmitService submitService) {
        this.submitService = submitService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            ResponseUtil.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        // Get username from the JWT authentication handler
        String username = (String) exchange.getAttribute("username");
        if (username == null) {
            ResponseUtil.sendResponse(exchange, 401, "Authentication failed.");
            return;
        }

        try (Reader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {

            // Convert JSON request body to DTO
            ExamSubmissionDTO submission = objectMapper.readValue(reader, ExamSubmissionDTO.class);
            int examId = submission.getExamId();

            // Delegate the core logic to the service class
            String jsonResponse = submitService.processExamSubmission(username, examId, submission);

            // Send the successful response from the service
            ResponseUtil.sendResponse(exchange, 200, jsonResponse);

        } catch (IllegalArgumentException e) {
            // Handle validation errors from the service
            LOGGER.warning("Submission validation failed: " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 400, "Bad Request - " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Internal Server Error: " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 500, "Internal Server Error.");
        }
    }
}
