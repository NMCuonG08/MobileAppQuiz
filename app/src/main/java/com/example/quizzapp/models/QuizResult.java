package com.example.quizzapp.models;

public class QuizResult {
    private String id;
    private String quizId;
    private String quizTitle;
    private int totalQuestions;
    private int correctAnswers;
    private int totalScore;
    private long timestamp;
    private String completionTime; // Thời gian hoàn thành quiz (formatted)

    public QuizResult() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.timestamp = System.currentTimeMillis();
    }

    public QuizResult(String quizId, String quizTitle, int totalQuestions,
                      int correctAnswers, int totalScore) {
        this();
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.totalScore = totalScore;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getQuizTitle() { return quizTitle; }
    public void setQuizTitle(String quizTitle) { this.quizTitle = quizTitle; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getCompletionTime() { return completionTime; }
    public void setCompletionTime(String completionTime) { this.completionTime = completionTime; }

    // Utility methods
    public double getAccuracyPercentage() {
        return totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100 : 0;
    }

    public String getScoreText() {
        return correctAnswers + "/" + totalQuestions;
    }
}