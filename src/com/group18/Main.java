package com.group18;

import com.group18.core.ResourceRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The gateway to the application, needed to start Jailscape
 *
 * @author riyagupta frasergrandfield danielturato nothandotshuma ethanpugh
 */
public class Main extends Application {

    /**
     * The main primary stage for the whole application
     */
    public static Stage primaryStage;

    /**
     * Used to start and initialize the game
     * @param primaryStage The primary stage of the game
     */
    @Override
    public void start(Stage primaryStage) {
        ResourceRepository.createResourceMap();
        primaryStage.setResizable(false);
        try {
            BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("/scenes/MessageOfTheDayDisplay.fxml"));
            Scene scene = new Scene(root, 600, 400);
            setPrimaryStage(primaryStage);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Message Of The Day");
            primaryStage.show();
       } catch (IOException e) {
           e.printStackTrace();
        }
    }

    /**
     * Set's the primary stage for the game
     * @param stage The new primary stage
     */
    private static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Get's the primary stage for the game
     * @return The primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Launch the game
     * @param args Various potential args
     */
    public static void main(String[] args) {
        launch(args);
    }


}