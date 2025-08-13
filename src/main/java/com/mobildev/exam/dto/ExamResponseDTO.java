package com.mobildev.exam.dto;

import java.util.List;

public class ExamResponseDTO {
    private int id;
    private String title;
    private String description;
    private int durationMinutes;
    private List<QuestionResponseDTO> questions;

    public ExamResponseDTO() {
    }

    // Constructor
    public ExamResponseDTO(int id, String title, String description, int durationMinutes, List<QuestionResponseDTO> questions) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.questions = questions;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public List<QuestionResponseDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponseDTO> questions) {
        this.questions = questions;
    }
}
