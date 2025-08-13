package com.mobildev.exam.server;

import com.mobildev.exam.dao.ExamDAO;
import com.mobildev.exam.db.DatabaseManager;
import com.mobildev.exam.handlers.ExamsHandler;
import com.mobildev.exam.handlers.JwtAuthHandler;
import com.mobildev.exam.handlers.LoginHandler;
import com.mobildev.exam.handlers.SubmitHandler;
import com.mobildev.exam.service.ExamService;
import com.mobildev.exam.service.SubmitService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HttpServerApp {
    private static final Logger LOGGER = Logger.getLogger(HttpServerApp.class.getName());

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // dependencies create
        ExamDAO examDAO = new ExamDAO();
        ExamService examService = new ExamService(examDAO);
        SubmitService submitService = new SubmitService();

        // define the endpoints
        server.createContext("/login", new LoginHandler());
        server.createContext("/exams", new JwtAuthHandler(new ExamsHandler(examService)));
        server.createContext("/submit", new JwtAuthHandler(new SubmitHandler(submitService)));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        LOGGER.info("Server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down server and database connections...");
            server.stop(0);
            DatabaseManager.shutdown();
            LOGGER.info("Shutdown complete.");
        }));
    }
}