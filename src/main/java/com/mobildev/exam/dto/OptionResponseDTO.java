package com.mobildev.exam.dto;

public class OptionResponseDTO {
    private int id;
    private int questionId;
    private String optionText;

    public OptionResponseDTO() {
    }

    public OptionResponseDTO(int id, int questionId, String optionText) {
        this.id = id;
        this.questionId = questionId;
        this.optionText = optionText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}

