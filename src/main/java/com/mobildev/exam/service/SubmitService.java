package com.mobildev.exam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobildev.exam.db.DatabaseManager;
import com.mobildev.exam.dto.ExamResultDTO;
import com.mobildev.exam.dto.ExamSubmissionDTO;
import com.mobildev.exam.dto.UserAnswerSubmissionDTO;
import com.mobildev.exam.model.QuestionType;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles the business logic for exam submission, including
 * fetching correct answers, scoring, and saving to the database.
 */
public class SubmitService {

    private static final Logger LOGGER = Logger.getLogger(SubmitService.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String processExamSubmission(String username, int examId, ExamSubmissionDTO submission)
            throws SQLException, IOException, IllegalArgumentException {

        int userId = getUserIdFromUsername(username);

        try (Connection conn = DatabaseManager.getConnection()) {
            double score = 0.0;
            int totalQuestions = submission.getAnswers().size();

            // Fetch correct answers from the database for scoring
            Map<Integer, Map<String, Object>> correctAnswersMap = getCorrectAnswers(conn, examId);

            // Save user answers and calculate score
            score = saveUserAnswersAndScore(conn, userId, examId, submission.getAnswers(), correctAnswersMap);

            // Save the final score to the results table
            double finalScore = totalQuestions > 0 ? (score / totalQuestions) * 100 : 0;
            saveResult(conn, userId, examId, finalScore);

            // Prepare and return the response DTO
            ExamResultDTO resultDto = new ExamResultDTO(
                    "Sınav başarıyla gönderildi ve puanlandı.",
                    new BigDecimal(finalScore).setScale(2, BigDecimal.ROUND_HALF_UP)
            );
            return objectMapper.writeValueAsString(resultDto);
        }
    }

    /**
     * Gets the user ID from the username from the database.
     */
    private int getUserIdFromUsername(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new IllegalArgumentException("Username not found in '" + username + "' database.");
    }

    /**
     * Fetches the correct answers for a given exam from the database.
     */
    private Map<Integer, Map<String, Object>> getCorrectAnswers(Connection conn, int examId) throws SQLException {
        String sqlCorrectAnswers = "SELECT q.id, q.question_type, q.correct_answer, o.id as option_id, o.is_correct " +
                "FROM questions q LEFT JOIN options o ON q.id = o.question_id " +
                "WHERE q.exam_id = ?";
        Map<Integer, Map<String, Object>> correctAnswersMap = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sqlCorrectAnswers)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("id");
                    Map<String, Object> questionData = correctAnswersMap.computeIfAbsent(questionId, k -> new HashMap<>());
                    questionData.put("type", rs.getString("question_type"));
                    if (QuestionType.CLASSIC.name().equals(rs.getString("question_type"))) {
                        questionData.put("correctAnswerText", rs.getString("correct_answer"));
                    } else if (QuestionType.MULTIPLE_CHOICE.name().equals(rs.getString("question_type"))) {
                        if (rs.getBoolean("is_correct")) {
                            questionData.put("correctOptionId", rs.getInt("option_id"));
                        }
                    }
                }
            }
        }
        return correctAnswersMap;
    }

    /**
     * Saves user's answers to the database and calculates the score.
     */
    private double saveUserAnswersAndScore(Connection conn, int userId, int examId, List<UserAnswerSubmissionDTO> answers, Map<Integer, Map<String, Object>> correctAnswersMap) throws SQLException {
        double score = 0.0;
        String sqlInsertAnswer = "INSERT INTO user_answers (user_id, exam_id, question_id, selected_option_id, answer_text) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsertAnswer)) {
            for (UserAnswerSubmissionDTO userAnswer : answers) {
                int questionId = userAnswer.getQuestionId();
                Map<String, Object> correctData = correctAnswersMap.get(questionId);

                if (correctData != null) {
                    String questionType = (String) correctData.get("type");

                    if ("CLASSIC".equals(questionType)) {
                        String correctAnswerText = (String) correctData.get("correctAnswerText");
                        if (userAnswer.getAnswerText() != null && correctAnswerText != null && correctAnswerText.equalsIgnoreCase(userAnswer.getAnswerText())) {
                            score += 1.0;
                        }
                    } else if ("MULTIPLE_CHOICE".equals(questionType)) {
                        Integer correctOptionId = (Integer) correctData.get("correctOptionId");
                        if (userAnswer.getSelectedOptionId() != null && correctOptionId != null && correctOptionId.equals(userAnswer.getSelectedOptionId())) {
                            score += 1.0;
                        }
                    }
                }

                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, examId);
                insertStmt.setInt(3, userAnswer.getQuestionId());
                if (userAnswer.getSelectedOptionId() != null) {
                    insertStmt.setInt(4, userAnswer.getSelectedOptionId());
                } else {
                    insertStmt.setNull(4, java.sql.Types.INTEGER);
                }
                insertStmt.setString(5, userAnswer.getAnswerText());
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
        return score;
    }

    /**
     * Saves the final score of the exam to the results table.
     */
    private void saveResult(Connection conn, int userId, int examId, double finalScore) throws SQLException {
        String sqlInsertResult = "INSERT INTO results (user_id, exam_id, score) VALUES (?, ?, ?)";
        try (PreparedStatement resultStmt = conn.prepareStatement(sqlInsertResult)) {
            resultStmt.setInt(1, userId);
            resultStmt.setInt(2, examId);
            resultStmt.setBigDecimal(3, new BigDecimal(finalScore).setScale(2, BigDecimal.ROUND_HALF_UP));
            resultStmt.executeUpdate();
        }
    }
}
