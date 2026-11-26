package com.blog.model;

import java.sql.Timestamp;

/**
 * Represents a Blog Post.
 */
public class Post {
    private int id;
    private int userId;
    private String authorName; // Retrieved via SQL JOIN with users table
    private String title;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor
    public Post() {
    }

    // Constructor for creating a post
    public Post(int userId, String title, String content) {
        this.userId = userId;
        this.title = title;
        this.content = content;
    }

    // Full constructor
    public Post(int id, int userId, String authorName, String title, String content, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.authorName = authorName;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId=" + userId +
                ", authorName='" + authorName + '\'' +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
