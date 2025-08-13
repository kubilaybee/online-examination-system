package com.mobildev.exam.service;

import com.mobildev.exam.dao.ExamDAO;
import com.mobildev.exam.dto.ExamListResponseDTO;
import com.mobildev.exam.dto.ExamResponseDTO;
import com.mobildev.exam.dto.OptionResponseDTO;
import com.mobildev.exam.dto.QuestionResponseDTO;
import com.mobildev.exam.model.QuestionType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ExamService {
    private static final Logger LOGGER = Logger.getLogger(ExamService.class.getName());
    private final ExamDAO examDAO;

    public ExamService(ExamDAO examDAO) {
        this.examDAO = examDAO;
    }

    /**
     * Retrieves a list of all exams, mapping the raw database data to a DTO list.
     *
     * @return An ExamListResponseDTO containing a list of ExamResponseDTOs.
     */
    public ExamListResponseDTO getAllExams() {
        LOGGER.log(Level.INFO, "Fetching all exams from the database.");
        try {
            List<Map<String, Object>> examRows = examDAO.getAllExams();

            // Using a stream to map the raw data into DTOs for a more concise and readable approach.
            List<ExamResponseDTO> exams = examRows.stream()
                    .map(row -> new ExamResponseDTO(
                            (int) row.get("exam_id"),
                            (String) row.get("title"),
                            (String) row.get("description"),
                            (int) row.get("duration_minutes"),
                            new ArrayList<>()
                    ))
                    .collect(Collectors.toList());

            LOGGER.log(Level.INFO, "Successfully fetched {0} exams.", exams.size());
            return new ExamListResponseDTO(exams);

        } catch (SQLException e) {
            // Log the full stack trace for better debugging.
            LOGGER.log(Level.SEVERE, "Database exception while fetching all exams.", e);
            return new ExamListResponseDTO(new ArrayList<>());
        }
    }

    /**
     * Retrieves a specific exam and its details (questions, options) by ID.
     * The method reconstructs the hierarchical data structure from the flat database result.
     *
     * @param examId The ID of the exam to retrieve.
     * @return An ExamResponseDTO with nested questions and options, or null if the exam is not found.
     */
    public ExamResponseDTO getExamResponseById(int examId) {
        LOGGER.log(Level.INFO, "Fetching details for exam with ID: {0}", examId);
        try {
            List<Map<String, Object>> examDetailsRows = examDAO.getExamDetails(examId);

            if (examDetailsRows.isEmpty()) {
                LOGGER.log(Level.WARNING, "No exam found with ID: {0}", examId);
                return null;
            }

            // Using maps to efficiently track and reconstruct the nested object hierarchy.
            // This avoids nested loops and improves performance for larger datasets.
            Map<Integer, QuestionResponseDTO> questionMap = new HashMap<>();
            ExamResponseDTO exam = null;

            for (Map<String, Object> row : examDetailsRows) {
                // Initialize the exam DTO from the first row only.
                if (Objects.isNull(exam)) {
                    exam = new ExamResponseDTO(
                            (int) row.get("exam_id"),
                            (String) row.get("title"),
                            (String) row.get("description"),
                            (int) row.get("duration_minutes"),
                            new ArrayList<>()
                    );
                }

                Integer questionId = (Integer) row.get("question_id");
                // A LEFT JOIN can return nulls, so we must check.
                if (questionId == null) {
                    continue;
                }

                QuestionResponseDTO question;
                // Check if the question is already in our map.
                if (questionMap.containsKey(questionId)) {
                    question = questionMap.get(questionId);
                } else {
                    // If not, create a new QuestionResponseDTO and add it to the map and the exam list.
                    QuestionType questionType = QuestionType.valueOf((String) row.get("question_type"));
                    question = new QuestionResponseDTO(
                            questionId,
                            (int) row.get("exam_id"),
                            (String) row.get("question_text"),
                            questionType,
                            new ArrayList<>()
                    );
                    questionMap.put(questionId, question);
                    // Add the new question to the exam's question list.
                    exam.getQuestions().add(question);

                }

                Integer optionId = (Integer) row.get("option_id");
                if (Objects.nonNull(optionId)) {
                    OptionResponseDTO option = new OptionResponseDTO(
                            optionId,
                            questionId,
                            (String) row.get("option_text")
                    );
                    question.getOptions().add(option);
                }
            }

            LOGGER.log(Level.INFO, "Successfully mapped exam with ID: {0}", examId);
            return exam;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database exception while fetching exam details for ID: " + examId, e);
            return null;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Invalid question type encountered for exam ID: " + examId, e);
            return null;
        }
    }
}