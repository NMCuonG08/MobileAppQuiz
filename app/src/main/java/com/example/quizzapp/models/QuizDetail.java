package com.example.quizzapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuizDetail {
    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("creatorName")
    private String creatorName;

    @SerializedName("category")
    private String categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("questions")
    private List<Object> questions;

    @SerializedName("image")
    private String imageUrl;
    @SerializedName("questionNumber")
    private int questionNumber;
    @SerializedName("rating")
    private float averageRating;

    @SerializedName("level")
    private String level;

    @SerializedName("published")
    private boolean published;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName != null ? creatorName : createdBy;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getQuestionCount() {
        return questionNumber > 0 ? questionNumber : 0;

    }

    public List<Object> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Object> questions) {
        this.questions = questions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }
}