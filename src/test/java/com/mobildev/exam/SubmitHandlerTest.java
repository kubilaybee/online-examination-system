package com.mobildev.exam;

import com.mobildev.exam.dto.ExamSubmissionDTO;
import com.mobildev.exam.handlers.SubmitHandler;
import com.mobildev.exam.service.SubmitService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class SubmitHandlerTest {

    private SubmitHandler submitHandler;

    @Mock
    private HttpExchange mockExchange;
    @Mock
    private SubmitService mockSubmitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        submitHandler = new SubmitHandler(mockSubmitService);
    }

    @Test
    void testHandle_successfulSubmission_shouldReturn200() throws IOException, Exception {
        // Hazırlık (Arrange)
        String username = "testUser";
        String validJson = "{\"examId\": 101, \"answers\": []}";
        String responseJson = "{\"message\":\"Exam submitted and scored successfully.\",\"finalScore\":95.00}";

        InputStream inputStream = new ByteArrayInputStream(validJson.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();

        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getAttribute("username")).thenReturn(username);
        when(mockExchange.getRequestBody()).thenReturn(inputStream);
        when(mockExchange.getResponseBody()).thenReturn(outputStream);
        when(mockExchange.getResponseHeaders()).thenReturn(new Headers());

        when(mockSubmitService.processExamSubmission(
                eq(username),
                anyInt(),
                any(ExamSubmissionDTO.class)))
                .thenReturn(responseJson);

        submitHandler.handle(mockExchange);

        verify(mockExchange).sendResponseHeaders(200, responseJson.length());

        assertEquals(responseJson, outputStream.toString().trim());

        ArgumentCaptor<ExamSubmissionDTO> dtoCaptor = ArgumentCaptor.forClass(ExamSubmissionDTO.class);
        verify(mockSubmitService).processExamSubmission(eq(username), eq(101), dtoCaptor.capture());
        assertEquals(101, dtoCaptor.getValue().getExamId());
    }

    @Test
    void testHandle_invalidMethod_shouldReturn405() throws IOException {
        when(mockExchange.getRequestMethod()).thenReturn("GET");
        when(mockExchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(mockExchange.getResponseHeaders()).thenReturn(new Headers()); // Gerekli düzeltme: Yanıt başlıklarını mock'luyoruz

        submitHandler.handle(mockExchange);

        String errorMessage = "Method Not Allowed";
        verify(mockExchange).sendResponseHeaders(405, errorMessage.length());
    }

    @Test
    void testHandle_unauthorizedUser_shouldReturn401() throws IOException {
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getAttribute("username")).thenReturn(null);
        when(mockExchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(mockExchange.getResponseHeaders()).thenReturn(new Headers()); // Gerekli düzeltme: Yanıt başlıklarını mock'luyoruz

        submitHandler.handle(mockExchange);

        String errorMessage = "Authentication failed.";
        verify(mockExchange).sendResponseHeaders(401, errorMessage.length());
    }

    @Test
    void testHandle_serviceThrowsIllegalArgumentException_shouldReturn400() throws IOException, Exception {
        String username = "testUser";
        String validJson = "{\"examId\": 101, \"answers\": []}";

        InputStream inputStream = new ByteArrayInputStream(validJson.getBytes());
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getAttribute("username")).thenReturn(username);
        when(mockExchange.getRequestBody()).thenReturn(inputStream);
        when(mockExchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(mockExchange.getResponseHeaders()).thenReturn(new Headers()); // Gerekli düzeltme: Yanıt başlıklarını mock'luyoruz

        String exceptionMessage = "Exam not found.";
        when(mockSubmitService.processExamSubmission(
                eq(username),
                anyInt(),
                any(ExamSubmissionDTO.class)))
                .thenThrow(new IllegalArgumentException(exceptionMessage));

        submitHandler.handle(mockExchange);

        String expectedResponse = "Bad Request - " + exceptionMessage;
        verify(mockExchange).sendResponseHeaders(400, expectedResponse.length());
    }

    @Test
    void testHandle_invalidJson_shouldReturn500() throws IOException, Exception {
        String username = "testUser";
        String invalidJson = "{\"examId\": 101, \"answers\": [}"; // Bozuk JSON

        InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes());
        when(mockExchange.getRequestMethod()).thenReturn("POST");
        when(mockExchange.getAttribute("username")).thenReturn(username);
        when(mockExchange.getRequestBody()).thenReturn(inputStream);
        when(mockExchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        when(mockExchange.getResponseHeaders()).thenReturn(new Headers()); // Gerekli düzeltme: Yanıt başlıklarını mock'luyoruz

        submitHandler.handle(mockExchange);

        String errorMessage = "Internal Server Error.";
        verify(mockExchange).sendResponseHeaders(500, errorMessage.length());
    }
}