package com.example.quizzapp.models;
import com.google.gson.annotations.SerializedName;

public class Quiz {
    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("image")
    private String imageUrl;

    @SerializedName("category")
    private String categoryId;

    // categoryName không có trong API JSON
    private String categoryName;

    @SerializedName("createdBy")
    private String creatorId;

    // creatorName không có trong API JSON
    private String creatorName;

    @SerializedName("level")
    private String level;

    @SerializedName("rating")
    private float rating;

    @SerializedName("published")
    private boolean published;

    @SerializedName("createAt")
    private String createdAt;

    // Các getter và setter phù hợp với kiểu dữ liệu mới
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

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName != null ? creatorName : creatorId;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Phương thức cho tương thích ngược với code hiện tại
    public int getQuestionCount() {
        return 5; // Giá trị mặc định vì API không có trường này
    }

    public float getAverageRating() {
        return rating;
    }
}