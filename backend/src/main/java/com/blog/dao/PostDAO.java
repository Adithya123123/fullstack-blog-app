package com.blog.dao;

import com.blog.config.DBConnection;
import com.blog.model.Post;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Post operations using standard JDBC and PreparedStatements.
 * Uses SQL JOIN to fetch author names along with posts.
 */
public class PostDAO {

    /**
     * Creates a new blog post in the database.
     * Sets the generated id on the post object.
     */
    public boolean createPost(Post post) throws SQLException {
        String sql = "INSERT INTO posts (user_id, title, content) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, post.getUserId());
            stmt.setString(2, post.getTitle());
            stmt.setString(3, post.getContent());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        post.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Retrieves all blog posts in newest-first order with author username.
     */
    public List<Post> getAllPosts() throws SQLException {
        String sql = "SELECT p.id, p.user_id, u.username AS author_name, p.title, p.content, p.created_at, p.updated_at " +
                     "FROM posts p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "ORDER BY p.created_at DESC";

        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                posts.add(mapRowToPost(rs));
            }
        }
        return posts;
    }

    /**
     * Searches posts by title keyword in newest-first order.
     */
    public List<Post> searchPosts(String keyword) throws SQLException {
        String sql = "SELECT p.id, p.user_id, u.username AS author_name, p.title, p.content, p.created_at, p.updated_at " +
                     "FROM posts p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.title LIKE ? " +
                     "ORDER BY p.created_at DESC";

        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapRowToPost(rs));
                }
            }
        }
        return posts;
    }

    /**
     * Retrieves a single post by its ID with author username.
     */
    public Post getPostById(int id) throws SQLException {
        String sql = "SELECT p.id, p.user_id, u.username AS author_name, p.title, p.content, p.created_at, p.updated_at " +
                     "FROM posts p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPost(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all posts written by a specific user (for My Posts page).
     */
    public List<Post> getPostsByUserId(int userId) throws SQLException {
        String sql = "SELECT p.id, p.user_id, u.username AS author_name, p.title, p.content, p.created_at, p.updated_at " +
                     "FROM posts p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "WHERE p.user_id = ? " +
                     "ORDER BY p.created_at DESC";

        List<Post> posts = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapRowToPost(rs));
                }
            }
        }
        return posts;
    }

    /**
     * Updates title and content of a post only if owned by the specified userId.
     */
    public boolean updatePost(int id, int userId, String title, String content) throws SQLException {
        String sql = "UPDATE posts SET title = ?, content = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setInt(3, id);
            stmt.setInt(4, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a post only if owned by the specified userId.
     */
    public boolean deletePost(int id, int userId) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper to map a ResultSet row into a Post model object.
     */
    private Post mapRowToPost(ResultSet rs) throws SQLException {
        return new Post(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("author_name"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }
}
