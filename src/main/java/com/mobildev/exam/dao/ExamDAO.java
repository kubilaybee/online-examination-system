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

public class ExamDAO {
    public List<Map<String, Object>> getExamDetails(int examId) throws SQLException {
        String sql = "SELECT e.id AS exam_id, e.title, e.description, e.duration_minutes, " +
                "q.id AS question_id, q.question_text, q.question_type, " +
                "o.id AS option_id, o.option_text " +
                "FROM exams e " +
                "LEFT JOIN questions q ON e.id = q.exam_id " +
                "LEFT JOIN options o ON q.id = o.question_id " +
                "WHERE e.id = ? " +
                "ORDER BY e.id, q.id, o.id";

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
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
        }
        return results;
    }
}
