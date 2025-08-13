package com.mobildev.exam.service;

import com.mobildev.exam.dao.ExamDAO;
import com.mobildev.exam.dto.ExamResponseDTO;
import com.mobildev.exam.dto.OptionResponseDTO;
import com.mobildev.exam.dto.QuestionResponseDTO;
import com.mobildev.exam.model.QuestionType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ExamService {
    private static final Logger LOGGER = Logger.getLogger(ExamService.class.getName());
    private final ExamDAO examDAO;

    public ExamService(ExamDAO examDAO) {
        this.examDAO = examDAO;
    }

    public ExamResponseDTO getExamResponseById(int examId) {
        try {
            List<Map<String, Object>> rawData = examDAO.getExamDetails(examId);
            if (rawData.isEmpty()) {
                return null;
            }

            Map<Integer, ExamResponseDTO> examMap = new HashMap<>();

            for (Map<String, Object> row : rawData) {
                int currentExamId = (int) row.get("exam_id");

                if (!examMap.containsKey(currentExamId)) {
                    ExamResponseDTO exam = new ExamResponseDTO(
                            currentExamId,
                            (String) row.get("title"),
                            (String) row.get("description"),
                            (int) row.get("duration_minutes"),
                            new ArrayList<>()
                    );
                    examMap.put(currentExamId, exam);
                }

                int questionId = (int) row.get("question_id");

                if (questionId == 0) continue;

                ExamResponseDTO currentExam = examMap.get(currentExamId);
                QuestionResponseDTO currentQuestion = null;

                for (QuestionResponseDTO q : currentExam.getQuestions()) {
                    if (q.getId() == questionId) {
                        currentQuestion = q;
                        break;
                    }
                }

                if (currentQuestion == null) {
                    QuestionType questionType = QuestionType.valueOf((String) row.get("question_type"));
                    currentQuestion = new QuestionResponseDTO(
                            questionId,
                            currentExamId,
                            (String) row.get("question_text"),
                            questionType,
                            new ArrayList<>()
                    );
                    currentExam.getQuestions().add(currentQuestion);
                }

                Integer optionId = (Integer) row.get("option_id");
                if (optionId != null && optionId != 0) {
                    OptionResponseDTO option = new OptionResponseDTO(
                            optionId,
                            questionId,
                            (String) row.get("option_text")
                    );
                    currentQuestion.getOptions().add(option);
                }
            }
            return examMap.values().iterator().next();

        } catch (SQLException e) {
            LOGGER.severe("Database exception: " + e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            LOGGER.severe("Invalid Question Type: " + e.getMessage());
            return null;
        }
    }
}
