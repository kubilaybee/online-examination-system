package com.mobildev.exam.dto;

import java.util.List;

public class ExamListResponseDTO {
    private List<ExamResponseDTO> exams;

    public ExamListResponseDTO(List<ExamResponseDTO> exams) {
        this.exams = exams;
    }

    public List<ExamResponseDTO> getExams() {
        return exams;
    }

    public void setExams(List<ExamResponseDTO> exams) {
        this.exams = exams;
    }
}
