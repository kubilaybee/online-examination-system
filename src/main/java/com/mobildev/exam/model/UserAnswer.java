package com.mobildev.exam.model;

public class UserAnswer {
    private int id;
    private int userId;
    private int examId;
    private int questionId;
    private Integer selectedOptionId; // for multiple choices maybe can be null
    private String answerText; // for classic question, maybe can be null

    public UserAnswer() {
    }

    public UserAnswer(int id, int userId, int examId, int questionId, Integer selectedOptionId, String answerText) {
        this.id = id;
        this.userId = userId;
        this.examId = examId;
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.answerText = answerText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
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
