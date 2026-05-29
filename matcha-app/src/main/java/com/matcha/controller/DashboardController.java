package com.matcha.controller;

import com.matcha.config.DatabaseConnection;
import com.matcha.model.MatchaLog;
import com.matcha.model.User;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * DashboardController — Manages the main dashboard scene.
 *
 * Features:
 *  - Animated counter button to track real-time matcha intake
 *  - Motivational header banner that dynamically updates based on total cup count
 *  - "Log Today's Drinks" commits the daily total to the database via UPSERT
 *  - JavaFX TableView rendering full matcha_logs history with inline record deletion
 */
public class DashboardController implements Initializable {

    // -------------------------------------------------------------------------
    // FXML-injected UI nodes
    // -------------------------------------------------------------------------

    @FXML private Label  welcomeLabel;
    @FXML private Label  motivationalLabel;
    @FXML private Button counterButton;
    @FXML private Button logButton;
    @FXML private Label  statusLabel;
    @FXML private Label  todayCountLabel;

    @FXML private TableView<MatchaLog>                     logsTable;
    @FXML private TableColumn<MatchaLog, String>           dateColumn;
    @FXML private TableColumn<MatchaLog, Integer>          countColumn;
    @FXML private TableColumn<MatchaLog, Void>             deleteColumn;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private User                          currentUser;
    private int                           sessionCount  = 0;
    private final ObservableList<MatchaLog> logs        = FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTable();
        logsTable.setItems(logs);
        logsTable.setPlaceholder(new Label("No matcha logs yet. Start sipping! \uD83C\uDF75"));
    }

    /**
     * Called by MainApp after FXML loading to inject the authenticated user
     * and populate the dashboard with live data.
     *
     * @param user the authenticated User from LoginController
     */
    public void initUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
        loadTodayCount();
        loadAllLogs();
        refreshMotivationalLabel();
    }

    // -------------------------------------------------------------------------
    // TableView setup
    // -------------------------------------------------------------------------

    private void configureTable() {
        // Date column — formats LocalDate as a readable string
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("logDate"));
        dateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    MatchaLog log = (MatchaLog) getTableRow().getItem();
                    setText(log.getLogDate() != null
                        ? log.getLogDate().toString()
                        : "—");
                }
            }
        });

        // Count column
        countColumn.setCellValueFactory(new PropertyValueFactory<>("drinkCount"));
        countColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item + " cup" + (item == 1 ? "" : "s"));
                setAlignment(Pos.CENTER);
            }
        });

        // Delete column — renders a pink Delete button per row
        deleteColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle(
                    "-fx-background-color: #FFCDD2;" +
                    "-fx-text-fill: #B71C1C;" +
                    "-fx-font-size: 11px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 5;" +
                    "-fx-border-radius: 5;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 4 10;"
                );
                deleteBtn.setOnAction(e -> {
                    MatchaLog log = getTableView().getItems().get(getIndex());
                    confirmAndDeleteLog(log);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                HBox container = new HBox(deleteBtn);
                container.setAlignment(Pos.CENTER);
                setGraphic(empty ? null : container);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Database operations
    // -------------------------------------------------------------------------

    /**
     * Loads today's existing count from the database (if any) to pre-populate
     * the session counter correctly on re-open.
     */
    private void loadTodayCount() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT drink_count FROM matcha_logs " +
                         "WHERE user_id = ? AND log_date = CURDATE() LIMIT 1";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentUser.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        sessionCount = rs.getInt("drink_count");
                    }
                }
            }
            updateCounterButton();

        } catch (SQLException e) {
            showStatus("Could not load today's count: " + e.getMessage(), false);
        }
    }

    /**
     * Loads all matcha log entries for the current user, ordered newest first.
     */
    private void loadAllLogs() {
        logs.clear();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "SELECT id, user_id, drink_count, log_date " +
                         "FROM matcha_logs WHERE user_id = ? " +
                         "ORDER BY log_date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentUser.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        logs.add(new MatchaLog(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("drink_count"),
                            rs.getDate("log_date").toLocalDate()
                        ));
                    }
                }
            }

        } catch (SQLException e) {
            showStatus("Could not load log history: " + e.getMessage(), false);
        }
    }

    // -------------------------------------------------------------------------
    // FXML action handlers
    // -------------------------------------------------------------------------

    /**
     * Increments the in-memory cup counter and animates the counter button.
     */
    @FXML
    private void handleIncrement() {
        sessionCount++;
        updateCounterButton();
        animateCounterButton();
        showStatus("", true); // clear any previous status
    }

    /**
     * Resets the in-memory counter to zero without touching the database.
     */
    @FXML
    private void handleReset() {
        if (sessionCount == 0) return;
        sessionCount = 0;
        updateCounterButton();
        showStatus("Counter reset to zero.", true);
    }

    /**
     * Commits the current session count to the database as today's log entry.
     * Uses INSERT … ON DUPLICATE KEY UPDATE for safe daily upsert.
     */
    @FXML
    private void handleLogDrinks() {
        if (sessionCount == 0) {
            showStatus("Tap the counter button first to log some cups! \uD83C\uDF75", false);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            String sql = "INSERT INTO matcha_logs (user_id, drink_count, log_date) " +
                         "VALUES (?, ?, CURDATE()) " +
                         "ON DUPLICATE KEY UPDATE drink_count = VALUES(drink_count)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, currentUser.getId());
                stmt.setInt(2, sessionCount);
                stmt.executeUpdate();
            }

            loadAllLogs();
            refreshMotivationalLabel();
            showStatus("Today's log saved — " + sessionCount + " cup" +
                       (sessionCount == 1 ? "" : "s") + " committed! \u2705", true);

        } catch (SQLException e) {
            showStatus("Failed to save log: " + e.getMessage(), false);
        }
    }

    /**
     * Shows a confirmation dialog before deleting a log record.
     * Refreshes the table and motivational label after successful deletion.
     */
    private void confirmAndDeleteLog(MatchaLog log) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Log Entry");
        alert.setHeaderText("Remove this matcha log?");
        alert.setContentText("Date: " + log.getLogDate() +
                             "\nCups: " + log.getDrinkCount() +
                             "\n\nThis action cannot be undone.");

        // Apply pastel styling to the dialog
        alert.getDialogPane().setStyle("-fx-background-color: #E8F5E9;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                String sql = "DELETE FROM matcha_logs WHERE id = ? AND user_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, log.getId());
                    stmt.setInt(2, currentUser.getId());
                    stmt.executeUpdate();
                }

                loadAllLogs();
                refreshMotivationalLabel();
                showStatus("Log entry for " + log.getLogDate() + " deleted.", true);

            } catch (SQLException e) {
                showStatus("Failed to delete log: " + e.getMessage(), false);
            }
        }
    }

    /**
     * Returns the user to the Login scene and clears session state.
     */
    @FXML
    private void handleLogout() {
        try {
            sessionCount = 0;
            logs.clear();
            MainApp.loadLoginScene();
        } catch (Exception e) {
            showStatus("Logout error: " + e.getMessage(), false);
        }
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    /**
     * Calculates the motivational banner message based on total lifetime cup count.
     * Thresholds: 0-2 cups | 3-5 cups | 6+ cups
     */
    private void refreshMotivationalLabel() {
        int total = logs.stream()
                        .mapToInt(MatchaLog::getDrinkCount)
                        .sum();

        String message;
        if (total <= 2) {
            message = "Mmm, matcha! \uD83C\uDF75";
        } else if (total <= 5) {
            message = "Matcha lover status! \uD83D\uDC95";
        } else {
            message = "Unstoppable green tea energy! \uD83D\uDD25";
        }

        motivationalLabel.setText(message);
    }

    private void updateCounterButton() {
        counterButton.setText(sessionCount + "\n\uD83C\uDF75");
    }

    /**
     * Plays a quick scale bounce animation on the counter button when tapped.
     */
    private void animateCounterButton() {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), counterButton);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.12);
        st.setToY(1.12);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        String color = success ? "#388E3C" : "#C62828";
        statusLabel.setStyle(
            "-fx-text-fill: " + color + ";" +
            "-fx-font-size: 12px;"
        );
    }
}
