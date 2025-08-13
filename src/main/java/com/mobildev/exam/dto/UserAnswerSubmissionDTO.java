package com.mobildev.exam.dto;

public class UserAnswerSubmissionDTO {
    private int questionId;
    private Integer selectedOptionId; // for MULTIPLE_CHOICE questions
    private String answerText; // for CLASSIC questions

    public UserAnswerSubmissionDTO() {
    }

    public UserAnswerSubmissionDTO(int questionId, Integer selectedOptionId, String answerText) {
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.answerText = answerText;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public Integer getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Integer selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}
