package com.blog.service;

import com.blog.dao.UserDAO;
import com.blog.model.User;
import com.blog.util.PasswordUtil;

import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Service class handling business logic and validation for User registration,
 * authentication, and profile lookup.
 */
public class UserService {

    private final UserDAO userDAO;

    // Email validation regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user after comprehensive validation.
     * Throws IllegalArgumentException for invalid input.
     * Throws IllegalStateException for duplicate username or email.
     */
    public User registerUser(String username, String email, String password) throws SQLException {
        // 1. Validate required fields
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        username = username.trim();
        email = email.trim().toLowerCase();

        // 2. Validate username length and characters
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }

        // 3. Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // 4. Validate password length
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        // 5. Check for duplicate username
        if (userDAO.isUsernameTaken(username)) {
            throw new IllegalStateException("Username is already taken");
        }

        // 6. Check for duplicate email
        if (userDAO.isEmailTaken(email)) {
            throw new IllegalStateException("Email address is already registered");
        }

        // 7. Hash password securely with BCrypt
        String hashedPassword = PasswordUtil.hashPassword(password);

        // 8. Create user in database
        User newUser = new User(username, email, hashedPassword);
        boolean created = userDAO.createUser(newUser);

        if (!created) {
            throw new SQLException("Failed to create user account");
        }

        // Clear password hash before returning
        newUser.setPasswordHash(null);
        return newUser;
    }

    /**
     * Authenticates user with email and password.
     * Returns the authenticated User if credentials are valid, or null otherwise.
     */
    public User authenticate(String email, String password) throws SQLException {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        email = email.trim().toLowerCase();
        User user = userDAO.findByEmail(email);

        if (user == null) {
            return null; // User not found
        }

        // Verify password against stored BCrypt hash
        boolean passwordMatches = PasswordUtil.checkPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            return null; // Invalid password
        }

        // Don't leak the password hash
        user.setPasswordHash(null);
        return user;
    }

    /**
     * Retrieves user by id without sensitive password hash.
     */
    public User getUserById(int id) throws SQLException {
        User user = userDAO.findById(id);
        if (user != null) {
            user.setPasswordHash(null);
        }
        return user;
    }
}
