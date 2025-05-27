package com.example.quizzapp.wrapper;

import com.example.quizzapp.models.QuizDetail;
import com.google.gson.annotations.SerializedName;

public class QuizDetailResponse {
    @SerializedName("quizze")
    private QuizDetail quizze;

    @SerializedName("questionNumber")
    private int questionNumber;
    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("players")
    private int players;
    @SerializedName("author")
    private String author;

    public QuizDetail getQuizze() {
        return quizze;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public int getPlayers() {
        return players;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategoryName(){
        return categoryName;
    }
}
