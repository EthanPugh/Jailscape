package com.group18.controller;

import com.group18.Main;
import com.group18.core.LevelSaver;
import com.group18.model.entity.User;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

/**
 * Each type of Alert Controller uses basic functionality from this class
 *
 * @author danielturato
 */
public abstract class AlertController extends BaseController {

    /**
     * The current level this Alert Controller is showing on
     */
    int currentLevel;

    /**
     * The current user selected in the game
     */
    private User user;

    /**
     * The current stage the alert is being shown upon
     */
    private Stage currentAlertStage;

    /**
     * Get the current level for this Menu Alert
     * @return The current level
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Set the current level for this Menu Alert
     * @param level The new current level
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    /**
     * Set the user associated with this Alert
     * @param user The new user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get the user associated with this Alert
     * @return The current selected user
     */
    public User getUser() {
        return user;
    }

    /**
     * Set the current Alert Stage associated with this controller
     * @param currentAlertStage The new Alert Stage
     */
    public void setCurrentAlertStage(Stage currentAlertStage) {
        this.currentAlertStage = currentAlertStage;
    }

    /**
     * Get the current Alert Stage associated with this controller
     * @return The current Alert Stage
     */
    public Stage getCurrentAlertStage() {
        return currentAlertStage;
    }

    /**
     * Each type of Alert Controller will require it's own setup method
     */
    public abstract void setup();

    /**
     * Upon clicking the reset button, the Alert will load the current level for the
     * current user.
     * @param actionEvent The even object containing information regarding the button click.
     */
    void handleResetLevelButton(ActionEvent actionEvent) {
        user.resetInventory(currentLevel);
        LevelSaver.delete("./src/resources/saved-levels/" +
                user.getUsername() + "-level-save" + currentLevel + ".txt");
        GameController.setUser(user);
        GameController.loadBaseLevel(currentLevel);
        MainMenuController.getMenuMusicPlayer().stop();
        currentAlertStage.hide();
        Main.getPrimaryStage().setTitle("Jailscape");
        GameController gc = new GameController(Main.getPrimaryStage());
    }

    /**
     * When clicking the cancel button, this alert will hide.
     * @param actionEvent The action even associated with this button click.
     */
    void handleCancelButton(ActionEvent actionEvent) {
        currentAlertStage.hide();
    }
}
