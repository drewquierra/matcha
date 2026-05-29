package com.matcha.controller;

import com.matcha.config.DatabaseConnection;
import com.matcha.model.User;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * LoginController — Handles authentication scene logic.
 *
 * Validates user credentials against the 'users' database table using
 * BCrypt password hash comparison via org.mindrot.jbcrypt.BCrypt.checkpw().
 * On success, delegates scene transition to MainApp.loadDashboardScene().
 */
public class LoginController implements Initializable {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         statusLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Allow pressing Enter in the password field to trigger login
        passwordField.setOnAction(event -> handleLogin());
        usernameField.setOnAction(event -> passwordField.requestFocus());

        // Fade-in the status label initially hidden
        statusLabel.setOpacity(0);
    }

    /**
     * Handles the Login button action.
     * Validates inputs, queries the database, and performs BCrypt hash verification.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String rawPassword = passwordField.getText();

        if (username.isEmpty() || rawPassword.isEmpty()) {
            showStatus("Please enter both your username and password.", false);
            return;
        }

        // Disable button to prevent double-submission
        loginButton.setDisable(true);
        loginButton.setText("Verifying...");

        // Run DB query off the JavaFX Application Thread
        new Thread(() -> {
            try {
                User authenticatedUser = authenticate(username, rawPassword);

                Platform.runLater(() -> {
                    if (authenticatedUser != null) {
                        try {
                            MainApp.loadDashboardScene(authenticatedUser);
                        } catch (Exception e) {
                            showStatus("Failed to load dashboard: " + e.getMessage(), false);
                            resetButton();
                        }
                    } else {
                        showStatus("Invalid username or password. Please try again.", false);
                        passwordField.clear();
                        passwordField.requestFocus();
                        resetButton();
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("Database error: " + e.getMessage(), false);
                    resetButton();
                });
            }
        }).start();
    }

    /**
     * Queries the database for the given username and verifies the raw password
     * against the stored BCrypt hash.
     *
     * @param username    plaintext username input
     * @param rawPassword plaintext password input
     * @return authenticated User object, or null if credentials are invalid
     */
    private User authenticate(String username, String rawPassword) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ? LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");

                    // BCrypt constant-time comparison — prevents timing attacks
                    if (BCrypt.checkpw(rawPassword, storedHash)) {
                        return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            storedHash
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * Displays a fade-in status message with colour-coded severity.
     *
     * @param message the status message to display
     * @param success true for a success/neutral message, false for an error
     */
    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        String color = success ? "#388E3C" : "#C62828";
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");

        FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void resetButton() {
        loginButton.setDisable(false);
        loginButton.setText("Log In");
    }
}
