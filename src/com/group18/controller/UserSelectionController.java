package com.group18.controller;

import com.group18.core.LevelSaver;
import com.group18.core.UserRepository;
import com.group18.model.entity.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.util.List;


/**
 * The controller which allows the user to select the user they wish to play the game with
 *
 * @author frasergrandfield
 */
public class UserSelectionController extends BaseController {

    /**
     * The button, once clicked will exit the user from the game
     */
    @FXML
    private Button exitButton;

    /**
     * The button, once clicked will use the selected user and re-direct to the Main Menu scene
     */
    @FXML
    private Button goButton;

    /**
     * The button, once clicked will load the User Creation scene
     */
    @FXML
    private Button createProfileButton;

    /**
     * The button, once clicked will delete the current selected user from the drop-down list
     */
    @FXML
    private Button deleteButton;

    /**
     * Used to display the list of all available selectable users
     */
    @FXML
    private ListView userListView;

    /**
     * An observable list, observing the listView of available user's to play with
     */
    private ObservableList observableList = FXCollections.observableArrayList();

    /**
     * The currently selected user's username
     */
    private String chosenUserName;

    /**
     * The possible selected user object
     */
    private User user;

    /**
     * Initialize this controller
     */
    public void initialize(){
        goButton.setOnAction(e -> {
            buttonClick();
            handleGoButtonAction();
        });

        createProfileButton.setOnAction(e -> {
            buttonClick();
            handleCreateButtonAction();
        });

        exitButton.setOnAction(e -> {
            buttonClick();
            handleExitButtonAction();
        });

        deleteButton.setOnAction(e -> {
            buttonClick();
            handleDeleteButtonAction();
        });

        loadData();
    }

    /**
     * Load all possible available user's into the list view
     */
    private void loadData(){
        userListView.getItems().clear();
        observableList.removeAll(observableList);
        List<User> userList = UserRepository.getAll();
        for (int i = 0; i < userList.size(); i++) {
            observableList.add(userList.get(i).getUsername());
        }
        userListView.getItems().addAll(observableList);
    }

    /**
     * Load the Main Menu if the user has selected a valid user to play with
     */
    private void handleGoButtonAction(){
       if (userListView.getSelectionModel().getSelectedItem() != null) {
           chosenUserName = (String) userListView.getSelectionModel().getSelectedItem();
           this.user = UserRepository.get("./src/resources/users/" + chosenUserName + ".ser");
           loadMainMenu(user);
       }
    }

    /**
     * Load the User Creation scene
     */
    private void handleCreateButtonAction(){
        loadFXMLScene("/scenes/NewUser.fxml", "Create User");
    }

    /**
     * Handles the exit the game
     */
    private void handleExitButtonAction(){
        Platform.exit();
        System.exit(0);
    }

    /**
     * Handles the deletion of a selected user from the list view
     */
    private void handleDeleteButtonAction() {
        if (userListView.getSelectionModel().getSelectedItem() != null) {
            chosenUserName = (String) userListView.getSelectionModel().getSelectedItem();
            UserRepository.delete(chosenUserName + ".ser");

            String baseSavedFileDir =
                    "./src/resources/saved-levels/" + chosenUserName + "-level-save";

            for (int i = 1; i <=5; i++) {
                LevelSaver.delete(baseSavedFileDir + i + ".txt");
            }

            loadData();
        }
    }

}
