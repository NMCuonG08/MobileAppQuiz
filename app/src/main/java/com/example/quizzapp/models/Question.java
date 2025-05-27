package com.example.quizzapp.models;


import java.util.List;

public class Question {
    private String _id;
    private String quizId;
    private String type;
    private String text;
    private List<Option> options;
    private String layout;
    private int point;
    private int time;
    private String description;
    private String media;
    private String mediaType;
    private String createAt;

    // Constructors
    public Question() {}

    // Getters and Setters
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<Option> getOptions() { return options; }
    public void setOptions(List<Option> options) { this.options = options; }

    public String getLayout() { return layout; }
    public void setLayout(String layout) { this.layout = layout; }

    public int getPoint() { return point; }
    public void setPoint(int point) { this.point = point; }

    public int getTime() { return time; }
    public void setTime(int time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMedia() { return media; }
    public void setMedia(String media) { this.media = media; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }
}


