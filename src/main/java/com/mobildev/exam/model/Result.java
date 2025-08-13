package com.mobildev.exam.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Result {
    private int id;
    private int userId;
    private int examId;
    private BigDecimal score;
    private LocalDateTime submissionDate;

    public Result() {
    }

    public Result(int id, int userId, int examId, BigDecimal score, LocalDateTime submissionDate) {
        this.id = id;
        this.userId = userId;
        this.examId = examId;
        this.score = score;
        this.submissionDate = submissionDate;
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

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }
}
