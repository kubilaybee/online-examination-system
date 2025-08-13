package com.mobildev.exam.dto;

import java.util.List;

public class ExamSubmissionDTO {
    private int userId;
    private int examId;
    private List<UserAnswerSubmissionDTO> answers;

    public ExamSubmissionDTO() {
    }

    public ExamSubmissionDTO(int userId, int examId, List<UserAnswerSubmissionDTO> answers) {
        this.userId = userId;
        this.examId = examId;
        this.answers = answers;
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

    public List<UserAnswerSubmissionDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<UserAnswerSubmissionDTO> answers) {
        this.answers = answers;
    }
}
