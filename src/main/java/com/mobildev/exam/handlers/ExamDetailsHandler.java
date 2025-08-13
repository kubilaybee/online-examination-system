package com.mobildev.exam.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobildev.exam.dto.ExamResponseDTO;
import com.mobildev.exam.service.ExamService;
import com.mobildev.exam.util.ResponseUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.logging.Logger;

public class ExamDetailsHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(ExamDetailsHandler.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExamService examService;

    public ExamDetailsHandler(ExamService examService) {
        this.examService = examService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            ResponseUtil.sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        int examId;
        try {
            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");

            if (parts.length < 3 || !parts[1].equals("exams")) {
                ResponseUtil.sendResponse(exchange, 400, "Bad Request - Invalid URL format. Expected /exams/{id}");
                return;
            }
            examId = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            ResponseUtil.sendResponse(exchange, 400, "Bad Request - Invalid exam ID.");
            return;
        }

        ExamResponseDTO examResponse = examService.getExamResponseById(examId);

        if (examResponse == null) {
            ResponseUtil.sendResponse(exchange, 404, "Exam not found.");
        } else {
            String jsonResponse = objectMapper.writeValueAsString(examResponse);
            ResponseUtil.sendResponse(exchange, 200, jsonResponse);
        }
    }
}