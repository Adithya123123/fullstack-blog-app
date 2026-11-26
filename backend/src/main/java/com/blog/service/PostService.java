package com.blog.service;

import com.blog.dao.PostDAO;
import com.blog.model.Post;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class handling business logic and authorization for blog posts.
 */
public class PostService {

    private final PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public PostService(PostDAO postDAO) {
        this.postDAO = postDAO;
    }

    /**
     * Creates a new post after validating title and content.
     */
    public Post createPost(int userId, String title, String content) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        title = title.trim();
        content = content.trim();

        if (title.length() > 255) {
            throw new IllegalArgumentException("Post title cannot exceed 255 characters");
        }

        Post post = new Post(userId, title, content);
        boolean created = postDAO.createPost(post);

        if (!created) {
            throw new SQLException("Failed to create blog post");
        }

        return post;
    }

    /**
     * Retrieves all published posts in newest-first order.
     */
    public List<Post> getAllPosts() throws SQLException {
        return postDAO.getAllPosts();
    }

    /**
     * Searches posts by title keyword.
     */
    public List<Post> searchPosts(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return postDAO.getAllPosts();
        }
        return postDAO.searchPosts(keyword.trim());
    }

    /**
     * Retrieves a single post by id.
     */
    public Post getPostById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid post ID");
        }
        return postDAO.getPostById(id);
    }

    /**
     * Retrieves all posts authored by a specific user.
     */
    public List<Post> getPostsByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return postDAO.getPostsByUserId(userId);
    }

    /**
     * Updates an existing post with author ownership verification.
     * Throws IllegalArgumentException if input is invalid.
     * Throws SecurityException if user is not the original author (403 Forbidden).
     * Returns false if post was not found (404 Not Found).
     */
    public boolean updatePost(int postId, int requestingUserId, String title, String content) throws SQLException {
        if (postId <= 0) {
            throw new IllegalArgumentException("Invalid post ID");
        }
        if (requestingUserId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        title = title.trim();
        content = content.trim();

        if (title.length() > 255) {
            throw new IllegalArgumentException("Post title cannot exceed 255 characters");
        }

        // Verify post exists and requesting user is the owner
        Post existing = postDAO.getPostById(postId);
        if (existing == null) {
            return false; // Post not found
        }

        if (existing.getUserId() != requestingUserId) {
            throw new SecurityException("Unauthorized: You are not the author of this post");
        }

        return postDAO.updatePost(postId, requestingUserId, title, content);
    }

    /**
     * Deletes an existing post with author ownership verification.
     * Throws SecurityException if user is not the author (403 Forbidden).
     * Returns false if post was not found (404 Not Found).
     */
    public boolean deletePost(int postId, int requestingUserId) throws SQLException {
        if (postId <= 0) {
            throw new IllegalArgumentException("Invalid post ID");
        }
        if (requestingUserId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Post existing = postDAO.getPostById(postId);
        if (existing == null) {
            return false; // Post not found
        }

        if (existing.getUserId() != requestingUserId) {
            throw new SecurityException("Unauthorized: You are not the author of this post");
        }

        return postDAO.deletePost(postId, requestingUserId);
    }
}
