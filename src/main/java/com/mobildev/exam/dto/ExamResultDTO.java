package com.mobildev.exam.dto;

import java.math.BigDecimal;

public class ExamResultDTO {

    private String message;
    private BigDecimal score;

    public ExamResultDTO() {
    }

    /**
     * Constructs an ExamResultDTO with a message and a final score.
     *
     * @param message A descriptive message about the submission result.
     * @param score   The calculated score of the exam.
     */
    public ExamResultDTO(String message, BigDecimal score) {
        this.message = message;
        this.score = score;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }
}