package com.mobildev.exam.dao;

import com.mobildev.exam.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) for managing exam-related database operations.
 * This class handles interactions with the 'exams', 'questions', and 'options' tables.
 */
public class ExamDAO {
    private static final Logger LOGGER = Logger.getLogger(ExamDAO.class.getName());

    /**
     * Retrieves a list of all exams with their basic details.
     *
     * @return A List of Maps, where each Map represents a single exam.
     * @throws SQLException if a database access error occurs.
     */
    public List<Map<String, Object>> getAllExams() throws SQLException {
        String query = "SELECT id AS exam_id, title, description, duration_minutes FROM exams ORDER BY id";
        return executeGetAllExamsQuery(query);
    }

    /**
     * Retrieves a detailed view of a specific exam, including all its questions and options.
     *
     * @param examId The ID of the exam to retrieve.
     * @return A List of Maps containing denormalized data for the exam, questions, and options.
     * @throws SQLException if a database access error occurs.
     */
    public List<Map<String, Object>> getExamDetails(int examId) throws SQLException {
        String query = """
                SELECT e.id AS exam_id, e.title, e.description, e.duration_minutes,
                       q.id AS question_id, q.question_text, q.question_type,
                       o.id AS option_id, o.option_text
                FROM exams e
                LEFT JOIN questions q ON e.id = q.exam_id
                LEFT JOIN options o ON q.id = o.question_id
                WHERE e.id = ?
                ORDER BY e.id, q.id, o.id
                """;
        return executeGetExamDetailsQuery(query, List.of(examId));
    }

    /**
     * Helper method to execute the query for all exams and map the results.
     *
     * @param query The SQL query to execute.
     * @return A List of Maps representing the exam results.
     * @throws SQLException if an error occurs during query execution.
     */
    private List<Map<String, Object>> executeGetAllExamsQuery(String query) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        // Using a try-with-resources block ensures that the Connection, PreparedStatement, and
        // ResultSet are automatically closed, which is a best practice to prevent resource leaks.
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            LOGGER.log(Level.INFO, "Executing query: {0}", query);

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("exam_id", rs.getInt("exam_id"));
                row.put("title", rs.getString("title"));
                row.put("description", rs.getString("description"));
                row.put("duration_minutes", rs.getInt("duration_minutes"));
                results.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query: " + query, e);
            throw e;
        }
        return results;
    }

    /**
     * Helper method to execute the query for exam details and map the results.
     *
     * @param query      The SQL query to execute.
     * @param parameters A List of objects to set as parameters. Can be null.
     * @return A List of Maps representing the detailed query results.
     * @throws SQLException if an error occurs during query execution.
     */
    private List<Map<String, Object>> executeGetExamDetailsQuery(String query, List<Object> parameters) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // If parameters are provided, set them in the prepared statement.
            if (Objects.nonNull(parameters)) {
                for (int i = 0; i < parameters.size(); i++) {
                    stmt.setObject(i + 1, parameters.get(i));
                }
            }

            LOGGER.log(Level.INFO, "Executing query: {0} with parameters: {1}", new Object[]{query, parameters});

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("exam_id", rs.getInt("exam_id"));
                    row.put("title", rs.getString("title"));
                    row.put("description", rs.getString("description"));
                    row.put("duration_minutes", rs.getInt("duration_minutes"));
                    row.put("question_id", rs.getInt("question_id"));
                    row.put("question_text", rs.getString("question_text"));
                    row.put("question_type", rs.getString("question_type"));
                    row.put("option_id", rs.getInt("option_id"));
                    row.put("option_text", rs.getString("option_text"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query: " + query, e);
            throw e;
        }
        return results;
    }
}