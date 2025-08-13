package com.mobildev.exam.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobildev.exam.dto.ExamListResponseDTO;
import com.mobildev.exam.dto.ExamResponseDTO;
import com.mobildev.exam.service.ExamService;
import com.mobildev.exam.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.logging.Logger;

public class ExamsHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(ExamsHandler.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExamService examService;

    public ExamsHandler(ExamService examService) {
        this.examService = examService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            ResponseUtil.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            String uri = exchange.getRequestURI().getPath();
            String[] pathParts = uri.split("/");

            if (pathParts.length == 2) {
                handleGetAllExams(exchange);
            } else if (pathParts.length == 3) {
                handleGetExamById(exchange, pathParts[2]);
            } else {
                ResponseUtil.sendResponse(exchange, 400, "Bad Request");
            }

        } catch (Exception e) {
            LOGGER.severe("Error in ExamsHandler: " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleGetAllExams(HttpExchange exchange) throws IOException {
        try {
            ExamListResponseDTO examListResponse = examService.getAllExams();
            String jsonResponse = objectMapper.writeValueAsString(examListResponse);
            ResponseUtil.sendResponse(exchange, 200, jsonResponse);
        } catch (Exception e) {
            LOGGER.severe("Error getting all exams: " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleGetExamById(HttpExchange exchange, String examIdString) throws IOException {
        try {
            int examId = Integer.parseInt(examIdString);
            ExamResponseDTO examDetail = examService.getExamResponseById(examId);

            if (examDetail != null) {
                String jsonResponse = objectMapper.writeValueAsString(examDetail);
                ResponseUtil.sendResponse(exchange, 200, jsonResponse);
            } else {
                ResponseUtil.sendResponse(exchange, 404, "Exam Not Found");
            }
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid exam ID format: " + examIdString);
            ResponseUtil.sendResponse(exchange, 400, "Invalid Exam ID Format");
        } catch (Exception e) {
            LOGGER.severe("Error getting exam details: " + e.getMessage());
            ResponseUtil.sendResponse(exchange, 500, "Internal Server Error");
        }
    }
}