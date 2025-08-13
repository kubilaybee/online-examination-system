package com.mobildev.exam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobildev.exam.db.DatabaseManager;
import com.mobildev.exam.dto.ExamResultDTO;
import com.mobildev.exam.dto.ExamSubmissionDTO;
import com.mobildev.exam.dto.UserAnswerSubmissionDTO;
import com.mobildev.exam.model.QuestionType;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Handles the business logic for exam submission, including
 * fetching correct answers, scoring, and saving to the database.
 * The core logic is to process a user's submitted answers, score them
 * against the correct answers from the database, and persist the results.
 */
public class SubmitService {

    private static final Logger LOGGER = Logger.getLogger(SubmitService.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    // SQL queries
    private static final String SQL_GET_USER_ID = "SELECT id FROM users WHERE username = ?";
    private static final String SQL_GET_CORRECT_ANSWERS =
            "SELECT q.id, q.question_type, q.correct_answer, o.id as option_id, o.is_correct " +
                    "FROM questions q LEFT JOIN options o ON q.id = o.question_id " +
                    "WHERE q.exam_id = ?";
    private static final String SQL_INSERT_ANSWER = "INSERT INTO user_answers (user_id, exam_id, question_id, selected_option_id, answer_text) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_RESULT = "INSERT INTO results (user_id, exam_id, score) VALUES (?, ?, ?)";

    // Log messages and error messages
    private static final String LOG_START_PROCESS = "Starting exam submission process for user: {} (Exam ID: {})";
    private static final String LOG_USER_ID_FETCHED = "User ID fetched: {}";
    private static final String LOG_CORRECT_ANSWERS_FETCHED = "{} correct answers fetched for exam ID: {}";
    private static final String LOG_RAW_SCORE = "Raw score calculated: {} out of {} questions.";
    private static final String LOG_FINAL_SCORE_SAVED = "Final score saved for user {}: {}";
    private static final String LOG_PROCESSING_ANSWER = "Processing answer for question ID: {}";
    private static final String LOG_CORRECT_ANSWER_NOT_FOUND = "Correct answer not found for question ID: {}. Skipping scoring for this question.";
    private static final String LOG_CURRENT_SCORE = "Current score: {}";
    private static final String MSG_SUBMISSION_SUCCESS = "Exam submitted and scored successfully.";
    private static final String ERR_USERNAME_NOT_FOUND = "Username not found in '%s' database.";

    // Constants for scoring logic
    private static final double CORRECT_ANSWER_SCORE = 1.0;
    private static final double INCORRECT_ANSWER_SCORE = 0.0;
    private static final double TOTAL_SCORE_MULTIPLIER = 100.0;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * The main entry point for processing an exam submission.
     * It orchestrates fetching user ID, retrieving correct answers,
     * scoring the submission, and saving the final result.
     *
     * @param username   The username of the submitting user.
     * @param examId     The ID of the exam being submitted.
     * @param submission The DTO containing the user's answers.
     * @return A JSON string representing the exam result.
     * @throws SQLException If a database access error occurs.
     * @throws IOException  If an error occurs while serializing the response.
     */
    public String processExamSubmission(String username, int examId, ExamSubmissionDTO submission)
            throws SQLException, IOException {

        LOGGER.info(LOG_START_PROCESS.replace("{}", username).replace("{}", String.valueOf(examId)));

        // Get user ID from username
        int userId = getUserIdFromUsername(username);
        LOGGER.info(LOG_USER_ID_FETCHED.replace("{}", String.valueOf(userId)));

        try (Connection conn = DatabaseManager.getConnection()) {
            double score = INCORRECT_ANSWER_SCORE;
            int totalQuestions = submission.getAnswers().size();

            // Fetch correct answers for the exam from the database
            Map<Integer, Map<String, Object>> correctAnswersMap = getCorrectAnswers(conn, examId);
            LOGGER.info(LOG_CORRECT_ANSWERS_FETCHED.replace("{}", String.valueOf(correctAnswersMap.size())).replace("{}", String.valueOf(examId)));

            // Save user answers and calculate the score
            score = saveUserAnswersAndScore(conn, userId, examId, submission.getAnswers(), correctAnswersMap);
            LOGGER.info(LOG_RAW_SCORE.replace("{}", String.valueOf(score)).replace("{}", String.valueOf(totalQuestions)));

            // Calculate final score based on the total score and save it
            double finalScore = totalQuestions > 0 ? (score / totalQuestions) * TOTAL_SCORE_MULTIPLIER : INCORRECT_ANSWER_SCORE;
            saveResult(conn, userId, examId, finalScore);
            LOGGER.info(LOG_FINAL_SCORE_SAVED.replace("{}", username).replace("{}", String.valueOf(finalScore)));

            // Create the result DTO and convert it to a JSON string
            ExamResultDTO resultDto = new ExamResultDTO(
                    MSG_SUBMISSION_SUCCESS,
                    new BigDecimal(finalScore).setScale(2, ROUNDING_MODE)
            );
            return objectMapper.writeValueAsString(resultDto);
        }
    }

    /**
     * Gets the user ID from the username from the database.
     *
     * @param username The username to look up.
     * @return The user ID.
     * @throws SQLException             If a database access error occurs.
     * @throws IllegalArgumentException If the username is not found.
     */
    private int getUserIdFromUsername(String username) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_GET_USER_ID)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new IllegalArgumentException(String.format(ERR_USERNAME_NOT_FOUND, username));
    }

    /**
     * Fetches the correct answers for a given exam from the database.
     * The method handles different question types (Classic and Multiple Choice)
     * and stores the relevant correct answer data in a map.
     *
     * @param conn   The active database connection.
     * @param examId The ID of the exam.
     * @return A map where the key is the question ID and the value is a map
     * containing the question type and the correct answer.
     * @throws SQLException If a database access error occurs.
     */
    private Map<Integer, Map<String, Object>> getCorrectAnswers(Connection conn, int examId) throws SQLException {
        Map<Integer, Map<String, Object>> correctAnswersMap = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_GET_CORRECT_ANSWERS)) {
            stmt.setInt(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("id");
                    Map<String, Object> questionData = correctAnswersMap.computeIfAbsent(questionId, k -> new HashMap<>());
                    String questionType = rs.getString("question_type");
                    questionData.put("type", questionType);

                    // Add the correct answer to the map based on the question type
                    if (QuestionType.CLASSIC.name().equals(questionType)) {
                        questionData.put("correctAnswerText", rs.getString("correct_answer"));
                    } else if (QuestionType.MULTIPLE_CHOICE.name().equals(questionType)) {
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
     * This method iterates through each user answer, checks it against the
     * correct answers map, and updates the score.
     *
     * @param conn              The active database connection.
     * @param userId            The ID of the user.
     * @param examId            The ID of the exam.
     * @param answers           A list of user's answers.
     * @param correctAnswersMap A map of correct answers for scoring.
     * @return The total score.
     * @throws SQLException If a database access error occurs.
     */
    private double saveUserAnswersAndScore(Connection conn, int userId, int examId, List<UserAnswerSubmissionDTO> answers, Map<Integer, Map<String, Object>> correctAnswersMap) throws SQLException {
        double score = INCORRECT_ANSWER_SCORE;
        try (PreparedStatement insertStmt = conn.prepareStatement(SQL_INSERT_ANSWER)) {
            for (UserAnswerSubmissionDTO userAnswer : answers) {
                int questionId = userAnswer.getQuestionId();
                LOGGER.info(LOG_PROCESSING_ANSWER.replace("{}", String.valueOf(questionId)));

                Map<String, Object> correctData = correctAnswersMap.get(questionId);
                if (Objects.isNull(correctData)) {
                    LOGGER.warning(LOG_CORRECT_ANSWER_NOT_FOUND.replace("{}", String.valueOf(questionId)));
                    continue;
                }

                // Calculate the score based on the question type
                score += calculateScoreForQuestion(userAnswer, correctData);
                LOGGER.info(LOG_CURRENT_SCORE.replace("{}", String.valueOf(score)));

                // Save the answer to the database
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, examId);
                insertStmt.setInt(3, questionId);
                if (Objects.nonNull(userAnswer.getSelectedOptionId())) {
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
     * Calculates the score for a single question based on its type and correct answer.
     * This helper method encapsulates the scoring logic for different question types.
     *
     * @param userAnswer  The user's submitted answer.
     * @param correctData The correct answer data for the question.
     * @return 1.0 if the answer is correct, otherwise 0.0.
     */
    private double calculateScoreForQuestion(UserAnswerSubmissionDTO userAnswer, Map<String, Object> correctData) {
        String questionType = (String) correctData.get("type");
        if (QuestionType.CLASSIC.name().equals(questionType)) {
            return scoreClassicQuestion(userAnswer, (String) correctData.get("correctAnswerText"));
        } else if (QuestionType.MULTIPLE_CHOICE.name().equals(questionType)) {
            return scoreMultipleChoiceQuestion(userAnswer, (Integer) correctData.get("correctOptionId"));
        }
        return INCORRECT_ANSWER_SCORE;
    }

    /**
     * Scores a classic type question.
     */
    private double scoreClassicQuestion(UserAnswerSubmissionDTO userAnswer, String correctAnswerText) {
        if (Objects.nonNull(userAnswer.getAnswerText()) &&
                Objects.nonNull(correctAnswerText) &&
                correctAnswerText.equalsIgnoreCase(userAnswer.getAnswerText())) {
            return CORRECT_ANSWER_SCORE;
        }
        return INCORRECT_ANSWER_SCORE;
    }

    /**
     * Scores a multiple-choice type question.
     */
    private double scoreMultipleChoiceQuestion(UserAnswerSubmissionDTO userAnswer, Integer correctOptionId) {
        if (Objects.nonNull(userAnswer.getSelectedOptionId()) &&
                Objects.nonNull(correctOptionId) &&
                correctOptionId.equals(userAnswer.getSelectedOptionId())) {
            return CORRECT_ANSWER_SCORE;
        }
        return INCORRECT_ANSWER_SCORE;
    }


    /**
     * Saves the final score of the exam to the results table.
     *
     * @param conn       The active database connection.
     * @param userId     The ID of the user.
     * @param examId     The ID of the exam.
     * @param finalScore The final calculated score.
     * @throws SQLException If a database access error occurs.
     */
    private void saveResult(Connection conn, int userId, int examId, double finalScore) throws SQLException {
        try (PreparedStatement resultStmt = conn.prepareStatement(SQL_INSERT_RESULT)) {
            resultStmt.setInt(1, userId);
            resultStmt.setInt(2, examId);
            resultStmt.setBigDecimal(3, new BigDecimal(finalScore).setScale(2, ROUNDING_MODE));
            resultStmt.executeUpdate();
        }
    }
}