package com.example.quizzapp.models;

public class Option {
    private String option;
    private boolean isCorrect;

    // Constructors
    public Option() {}

    public Option(String option, boolean isCorrect) {
        this.option = option;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}