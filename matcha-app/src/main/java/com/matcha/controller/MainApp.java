package com.matcha.controller;

import com.matcha.config.DatabaseConnection;
import com.matcha.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp — JavaFX Application entry point.
 * Acts as the central scene router, managing transitions between
 * the Login and Dashboard views.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Matcha Tracker \uD83C\uDF75");
        primaryStage.setResizable(false);
        loadLoginScene();
        primaryStage.show();
    }

    /**
     * Loads and displays the Login authentication scene.
     */
    public static void loadLoginScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/matcha/view/login.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 480, 380);
        primaryStage.setScene(scene);
    }

    /**
     * Loads and displays the Dashboard scene, injecting the authenticated User.
     *
     * @param user the currently authenticated User model
     */
    public static void loadDashboardScene(User user) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/com/matcha/view/dashboard.fxml")
        );
        Parent root = loader.load();

        DashboardController controller = loader.getController();
        controller.initUser(user);

        Scene scene = new Scene(root, 820, 660);
        primaryStage.setScene(scene);
    }

    /**
     * Returns the primary application Stage for external reference.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
