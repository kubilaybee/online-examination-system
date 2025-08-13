package com.mobildev.exam.dto;

import com.mobildev.exam.model.QuestionType;
import java.util.List;

public class QuestionResponseDTO {
    private int id;
    private int examId;
    private String questionText;
    private QuestionType questionType;
    private List<OptionResponseDTO> options;

    public QuestionResponseDTO() {
    }

    // Constructor
    public QuestionResponseDTO(int id, int examId, String questionText, QuestionType questionType, List<OptionResponseDTO> options) {
        this.id = id;
        this.examId = examId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.options = options;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public List<OptionResponseDTO> getOptions() {
        return options;
    }

    public void setOptions(List<OptionResponseDTO> options) {
        this.options = options;
    }
}
