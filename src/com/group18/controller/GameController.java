package com.group18.controller;


import com.group18.Main;
import com.group18.core.LevelLoader;
import com.group18.core.LevelSaver;
import com.group18.core.ResourceRepository;
import com.group18.core.UserRepository;
import com.group18.exception.InvalidLevelException;
import com.group18.exception.InvalidMoveException;
import com.group18.model.Direction;
import com.group18.model.ElementType;
import com.group18.model.Level;
import com.group18.model.State;
import com.group18.model.cell.*;
import com.group18.model.entity.*;
import com.group18.model.item.Collectable;
import com.group18.model.item.ElementItem;
import com.group18.model.item.Key;
import com.group18.viewmodel.EnemyViewModel;
import com.group18.viewmodel.ItemViewModel;
import com.group18.viewmodel.UserViewModel;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;

/**
 * The controller for JailScape which controls the state and action
 * for each possible playable level.
 *
 * @author frasergrandfield danielturato ethanpugh
 */
public class GameController extends BaseController {

    /**
     * The width of each cell in each level. Also used to determine
     * how far each entity can move in speed.
     */
    private static final int CELL_WIDTH = 64 ;

    /**
     * A logger which allows specific output to the console
     */
    private static final Logger LOGGER = Logger.getLogger("GameController");

    /**
     * The level object associated with this level
     */
    private static Level level;

    /**
     * The user view model associated with this level
     */
    private static UserViewModel userViewModel;

    /**
     * The board pane for this controller
     */
    private static Pane boardPane;

    /**
     * The primary stage for this controller
     */
    private static Stage primaryStage;

    /**
     * The initial epoch start timeAnimation for this level
     */
    private static Instant startTime;

    /**
     * The total elapsed pause timeAnimation for this level
     */
    private static Long totalPausedTime = 0L;

    /**
     * The total saved timeAnimation used if this level comes from a save
     */
    private static Long totalSavedTime = 0L;

    /**
     * The current level number for this controller
     */
    private static int currentLevel;

    /**
     * The group which holds all images of all cells
     */
    private static Group cellImages;

    /**
     * The background music player for the game
     */
    private static MediaPlayer backgroundMusicPlayer;

    /**
     * The pane showing the users inventory
     */
    private static Pane inventoryPane;

    /**
     * The inventory item images being displayed to the user
     */
    private static Group inventoryItems;

    /**
     * Displays the number of tokens the user has
     */
    private static Text tokens;

    /**
     * Holds if the current animation of a user has been completed
     */
    private boolean animationCompleted = true;

    /**
     * The enemy view models associated with this level
     */
    private List<EnemyViewModel> enemyViewModels = new ArrayList<>();

    /**
     * The item view models associated with this level
     */
    private List<ItemViewModel> itemViewModels = new ArrayList<>();

    /**
     * The current width of this level
     */
    private int levelWidth;

    /**
     * The current height of this level
     */
    private int levelHeight;

    /**
     * Used to determine if the user pressed a button
     */
    private boolean pressed = false;

    /**
     * The current seconds passed, to be display to the user
     */
    private int seconds = 0;

    /**
     * The animation Timeline controlling the display of elapsed time
     */
    private Timeline timeAnimation;


    /**
     * Default constructor, needed in various circumstances
     */
    public GameController() {

    }

    /**
     * Creates a new Game Controller, starting the Game for the user at the
     * specified stage
     * @param stage The primary stage for the game to be played on
     */
    public GameController(Stage stage) {
        primaryStage = stage;
        init();
    }

    /**
     * Load a level from a base level file
     * @param levelNum The number of the level
     */
    public static void loadBaseLevel(int levelNum) {
        currentLevel = levelNum;
        level = LevelLoader.loadLevel(levelNum, userViewModel.getUser());
    }

    /**
     * Load a level from a saved level file
     * @param levelNum The number of the level
     */
    public static void loadSavedLevel(int levelNum) {
        currentLevel = levelNum;
        level = LevelLoader.loadSavedLevel(levelNum, userViewModel.getUser());
    }

    /**
     * Replace a cell image at a specific position with a ground image
     * @param point The position of the image
     */
    public static void replaceCell(Point point) {
        ImageView imageView = new ImageView(new Image(ResourceRepository.getResource("Ground")));
        imageView.setY(point.getY() * 64);
        imageView.setX(point.getX() * 64);

        int idx = (int) ((int)(level.getBoardHeight() * point.getY()) + point.getX());
        cellImages.getChildren().set(idx, imageView);
    }

    /**
     * Trigger a message alert with also a change in state
     * @param message the message to be shown to the user
     * @param state The state that is being changed
     */
    public void triggerAlert(String message, State state) {
        backgroundMusicPlayer.stop();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        User user = userViewModel.getUser();
        if (state == State.LEVEL_LOST) {
            playSound("PlayerDeath");
            user.resetInventory(level.getCurrentLevel());
            String baseSavedFileDir = "./src/resources/saved-levels/" +
                                      userViewModel.getUser().getUsername() + "-level-save" +
                                      currentLevel + ".txt";
            LevelSaver.delete(baseSavedFileDir);
            alert.setHeaderText("LEVEL LOST");
            alert.setContentText(message);
        } else if (state == State.LEVEL_WON) {
            playSound("LevelWin");
            Long finishTime = addNewFinishTime();
            String time = getFormattedTime(finishTime);
            alert.setContentText("You beat this level in " + time);
            alert.setHeaderText(message);
            user.setCurrentCell(null);
            if (user.getHighestLevel() == currentLevel) {
                if (currentLevel < 5) {
                    user.incrementLevel();
                }
            }
            user.resetInventory(currentLevel);
            UserRepository.save(user);
        }
        alert.showAndWait();
        Main.getPrimaryStage().setTitle("Main Menu");
        loadMainMenu(user);
    }

    /**
     * Set the user for this game
     * @param user The user
     */
    public static void setUser(User user) {
        userViewModel = new UserViewModel(user);
    }


    /**
     * Set the background music player for this controller
     * @param backgroundMusicPlayer The new media player
     */
    public static void setBackgroundMusicPlayer(MediaPlayer backgroundMusicPlayer) {
        GameController.backgroundMusicPlayer = backgroundMusicPlayer;
    }

    /**
     * Set the total timeAnimation this level received from a saved file
     * @param totalSavedTime The new total saved time
     */
    public static void setTotalSavedTime(Long totalSavedTime) {
        GameController.totalSavedTime = totalSavedTime;
    }

    /**
     * Set the number of tokens being displayed
     */
    public static void setTokens() {
        tokens.setText("Tokens: " + userViewModel.getUser().getTokens());
    }

    /**
     * Display the user's current inventory items
     */
    public static void displayInventoryItems() {
        User user = userViewModel.getUser();
        List<Collectable> inventory = user.getInventory(currentLevel);
        inventoryItems.getChildren().clear();
        inventoryPane.getChildren().remove(inventoryItems);

        for (int i = 0; i < inventory.size(); i++) {
            Collectable item = inventory.get(i);
            ImageView itemImage = new ImageView();
            itemImage.setX(i * 64);

            if (item instanceof ElementItem) {
                switch ((ElementItem) item) {
                    case ICE_SKATES:
                        itemImage.setImage(new Image(ResourceRepository.getResource("IceSkates")));
                        break;
                    case FLIPPERS:
                        itemImage.setImage(new Image(ResourceRepository.getResource("Flippers")));
                        break;
                    case FIRE_BOOTS:
                        itemImage.setImage(new Image(ResourceRepository.getResource("FireBoots")));
                        break;
                    default:
                        break;
                }
            } else {
                switch ((Key) item) {
                    case YELLOW_KEY:
                        itemImage.setImage(new Image(ResourceRepository.getResource("Key-Yellow")));
                        break;
                    case GREEN_KEY:
                        itemImage.setImage(new Image(ResourceRepository.getResource("Key-Green")));
                        break;
                    case BLUE_KEY:
                        itemImage.setImage(new Image(ResourceRepository.getResource("Key-Blue")));
                        break;
                    case RED_KEY:
                        itemImage.setImage(new Image(ResourceRepository.getResource("Key-Red")));
                        break;
                    default:
                        break;
                }
            }
            inventoryItems.getChildren().add(itemImage);
        }
        inventoryPane.getChildren().add(inventoryItems);
    }

    /**
     * The init method, that will initialise the whole game in which the user
     * will be able to comfortably be able to play
     */
    private void init() {
        createBoard();
        createInventoryPane();
        displayInventory();

        if (userViewModel.getUser().getInventory(currentLevel).size() > 0) {
            displayInventoryItems();
        } else {
            inventoryPane.getChildren().add(inventoryItems);
        }

        Scene scene = setupDisplay();

        scene.setOnKeyPressed(e -> processKey(e.getCode()));
        scene.setOnKeyReleased(e -> pressed = false);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        startTime = Instant.now();
        playSound("BackgroundMusic");
    }

    /**
     * Setup the inventory & game display Scene
     * @return The scene containing the inventory and game views
     */
    private Scene setupDisplay() {
        tokens = new Text(409, 26, "Tokens: " + Integer.toString(userViewModel.getUser().getTokens()));
        tokens.setFill(Color.GOLD);
        String timeText = getStartingTimeDisplay();

        Text timeDisplay = new Text(408, 52, timeText);
        timeDisplay.setFill(Color.WHITE);
        timeDisplay.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        animateTime(timeDisplay);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(boardPane);
        inventoryPane.getChildren().addAll(timeDisplay, tokens);
        borderPane.setTop(inventoryPane);
        Scene scene = new Scene(borderPane,500,550);
        restrictView(scene);
        return scene;
    }

    /**
     * Get the starting time to be displayed to the user
     * @return The starting time to be displayed
     */
    private String getStartingTimeDisplay() {
        String timeText = "00:00";
        if (totalSavedTime != 0) {
            seconds = (int) (long) totalSavedTime / 1000;;
            int mins = seconds / 60;
            int secs = seconds % 60;
            timeText = String.format("%02d:%02d", mins, secs);
        }
        return timeText;
    }

    /**
     * Animate the time being display to the user
     * @param timeDisplay The text in which the time will be display upon
     */
    private void animateTime(Text timeDisplay) {
        timeAnimation = new Timeline();
        timeAnimation.setCycleCount(Animation.INDEFINITE);
        timeAnimation.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            seconds += 1;
            int mins = seconds / 60;
            int secs = seconds % 60;
            timeDisplay.setText(String.format("%02d:%02d", mins, secs));
        }));
        timeAnimation.play();
    }

    /**
     * Create the inventory pane
     */
    private void createInventoryPane() {
        inventoryPane = new Pane();
        inventoryItems = new Group();
        inventoryPane.setStyle("-fx-background-color: grey; -fx-border-color: black");
        inventoryPane.setMaxWidth(500);
        inventoryPane.setPrefSize(100,64);
    }

    /**
     * Display the inventory images
     */
    private void displayInventory() {
        Group inventory = new Group();
        for (int i = 0; i < 6; i++) {
            ImageView iv = new ImageView(new Image("resources/assets/Item/WoodPlanks.png"));
            iv.setX(i * 64);
            inventory.getChildren().addAll(iv);
        }

        inventoryPane.getChildren().add(inventory);
    }


    /**
     * Restrict the view, so the user can only view a specific section of the game
     * @param scene The scene that needs restricting
     */
    private void restrictView(Scene scene) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(scene.widthProperty());
        clip.heightProperty().bind(scene.heightProperty());

        ImageView userImageView = userViewModel.getImageView();
        clip.xProperty().bind(Bindings.createDoubleBinding(() ->
                        clampRange(userImageView.getX() - scene.getWidth() / 2,
                                0, boardPane.getWidth() - scene.getWidth()),
                                    userImageView.xProperty(), scene.widthProperty()));

        clip.yProperty().bind(Bindings.createDoubleBinding(() ->
                        clampRange(
                                userImageView.getY() - scene.getHeight() / 2,
                                0, boardPane.getHeight() - scene.getHeight()),
                                userImageView.yProperty(), scene.heightProperty()));

        boardPane.setClip(clip);
        boardPane.translateXProperty().bind(clip.xProperty().multiply(-1));
        boardPane.translateYProperty().bind(clip.yProperty().multiply(-1));
    }

    /**
     * Create and fill the whole board for this specific level
     */
    private void createBoard() {
        Pane pane = drawCells(level);
        boardPane = pane;
        drawAssets(pane, level);
    }

    /**
     * Draw each entity onto the level
     * @param pane The pane the entities will be drawn on
     * @param level The level object that these entities are on
     */
    private void drawAssets(Pane pane, Level level) {
        Cell[][] cells = level.getBoard();

        Group sprites = new Group();
        for (int i = 0; i < levelHeight; i++) {
            for (int j = 0; j < levelWidth; j++) {
                Cell cell = cells[j][i];
                if (cell.getCurrentEntities().size() > 0) {
                    Entity entity = cell.getCurrentEntities().get(0);
                    if (entity instanceof User) {
                        userViewModel.setImageView(j, i);
                        sprites.getChildren().add(userViewModel.getImageView());
                    } else {
                        EnemyViewModel enemyViewModel = createEnemyViewModel((Enemy) entity, j, i);
                        enemyViewModels.add(enemyViewModel);
                        sprites.getChildren().add(enemyViewModel.getImageView());
                    }
                }

                if (cell instanceof Ground) {
                    Ground ground = (Ground) cell;
                    if (ground.hasItem()) {
                        ItemViewModel itemViewModel = createItemViewModel(ground.getItem(), j, i);
                        itemViewModels.add(itemViewModel);
                        sprites.getChildren().addAll(itemViewModel.getImageView());
                    }
                }
            }
        }

        pane.getChildren().add(sprites);
    }

    /**
     * Creates a new Item View Model based on an item on a cell
     * @param item The item
     * @param y The item's Y position
     * @param x The item's X position
     * @return The ItemViewModel
     */
    private ItemViewModel createItemViewModel(Collectable item, int y, int x) {
        ItemViewModel itemViewModel = injectItemImages(item);
        itemViewModel.setImageView(y, x);
        return itemViewModel;
    }

    /**
     * Inject the required images for each item
     * @param item The item
     * @return The view model of the item
     */
    private ItemViewModel injectItemImages(Collectable item) {
        ItemViewModel itemViewModel = null;

        if (item instanceof ElementItem) {
            switch (((ElementItem) item)) {
                case FIRE_BOOTS:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("FireBoots")), item);
                    break;
                case FLIPPERS:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("Flippers")), item);
                    break;
                case ICE_SKATES:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("IceSkates")), item);
                    break;
                default:
                    break;
            }
        } else {
            switch (((Key) item)) {
                case RED_KEY:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("Key-Red")), item);
                    break;
                case BLUE_KEY:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("Key-Blue")), item);
                    break;
                case GREEN_KEY:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("Key-Green")), item);
                    break;
                case YELLOW_KEY:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("Key-Yellow")), item);
                    break;
                case TOKEN_KEY:
                    itemViewModel =
                            new ItemViewModel(new Image(ResourceRepository.getResource("Token")), item);
                    break;
                default:
                    break;
            }
        }
        return itemViewModel;
    }

    /**
     * Creates a new EnemyViewModel based on an Enemy object
     * @param enemy The enemy object
     * @param y It's Y position
     * @param x It's X position
     * @return The EnemyViewModel
     */
    private EnemyViewModel createEnemyViewModel(Enemy enemy, int y, int x) {
        EnemyViewModel enemyViewModel = injectEnemyImages(enemy);
        enemyViewModel.setImageView(y, x);

        return enemyViewModel;
    }

    /**
     * Draw each cell into a new pane
     * @param level The level object associated with this level
     * @return A pane containing all the cell images associated with this level
     */
    private Pane drawCells(Level level) {
        Pane board = new Pane();
        Cell[][] cells = level.getBoard();
        int boardWidth = level.getBoardWidth();
        int boardHeight = level.getBoardHeight();
        setBoardArea(boardWidth, boardHeight);

        Group cellGroups = new Group();
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                Cell cell = cells[i][j];
                ImageView imageView = createSpriteImageView(i, j, cell);
                cellGroups.getChildren().add(imageView);
            }
        }

        cellImages = cellGroups;
        board.getChildren().add(cellGroups);
        setBoardLimits(board);

        return board;
    }

    /**
     * Set's the board's min & max size limits
     * @param board The board pane associated with this level
     */
    private void setBoardLimits(Pane board) {
        board.setMinSize(levelWidth * 64, levelHeight * 64);
        board.setPrefSize(levelWidth * 64, levelHeight * 64);
        board.setMaxSize(levelWidth * 64, levelHeight * 64);
    }

    /**
     * Create a sprite image view, constraining it's width,height and
     * setting it's relative position
     * @param i The X position
     * @param j The Y position
     * @param cell The cell this imageView will be associated with
     * @return An imageview of the cell
     */
    private ImageView createSpriteImageView(int i, int j, Cell cell) {
        Image spriteImage = getSpriteImage(cell);

        ImageView imageView = new ImageView(spriteImage);
        imageView.setFitHeight(64);
        imageView.setFitWidth(64);
        imageView.setX(j * 64);
        imageView.setY(i * 64);
        return imageView;
    }

    /**
     * Get the image resource of a cell, depending on it's type
     * @param cell The cell we want the image off
     * @return The image of the cell
     */
    private Image getSpriteImage(Cell cell) {
        Image spriteImage = null;
        if (cell instanceof ColourDoor) {
            switch (((ColourDoor) cell).getColour()) {
                case RED:
                    spriteImage = new Image(ResourceRepository.getResource("Red-Door"));
                    break;
                case BLUE:
                    spriteImage = new Image(ResourceRepository.getResource("Blue-Door"));
                    break;
                case YELLOW:
                    spriteImage = new Image(ResourceRepository.getResource("Yellow-Door"));
                    break;
                case GREEN:
                    spriteImage = new Image(ResourceRepository.getResource("Green-Door"));
                    break;
                default:
                    break;
            }
        } else if (cell instanceof TokenDoor) {
            spriteImage = new Image(ResourceRepository.getResource("Token-Door"));
        } else if (cell instanceof Wall) {
            spriteImage = new Image(ResourceRepository.getResource("Wall"));
        } else if (cell instanceof Element) {
            switch (((Element) cell).getElementType()) {
                case FIRE:
                    spriteImage = new Image(ResourceRepository.getResource("Element-Fire"));
                    break;
                case WATER:
                    spriteImage = new Image(ResourceRepository.getResource("Element-Water"));
                    break;
                case ICE:
                    spriteImage = new Image(ResourceRepository.getResource("Element-Ice"));
                    break;
                default:
                    break;
            }
        } else if (cell instanceof Goal) {
            spriteImage = new Image(ResourceRepository.getResource("Goal"));
        } else if (cell instanceof Teleporter) {
            spriteImage = new Image(ResourceRepository.getResource("Teleporter"));
        } else if (cell instanceof Ground) {
            spriteImage = new Image(ResourceRepository.getResource("Ground"));
        }

        return spriteImage;
    }

    /**
     * Move the player in the game
     * @param deltaX The user's X translation value
     * @param deltaY The user's Y translation value
     */
    private void movePlayer(int deltaX, int deltaY) {
        ImageView userImageView = userViewModel.getImageView();
        User user = userViewModel.getUser();
        Cell userCurrentCell = user.getCurrentCell();

        pressed = true;
        double x = (clampRange(userImageView.getX() + deltaX,
                        0, boardPane.getWidth() - userImageView.getFitWidth()));

        double y =
                (clampRange(userImageView.getY() + deltaY,
                        0, boardPane.getHeight() - userImageView.getFitHeight()));


        if (userCurrentCell instanceof Element) {
            if (((Element) userCurrentCell).getElementType().equals(ElementType.ICE)) {
                animatedBasedOnPosition(userImageView, userCurrentCell);
            } else {
                animateUser(userImageView, x, y);
            }
        } else if (!(userCurrentCell instanceof Teleporter)) {
            animateUser(userImageView, x, y);
        } else {
            userImageView.setVisible(false);
            animatedBasedOnPosition(userImageView, userCurrentCell);
        }

        if (userCurrentCell instanceof Goal) {
            triggerAlert("Congratulations! You have completed Level " + currentLevel, State.LEVEL_WON);
        }

        checkForItemPickups(x, y);

        if (userCurrentCell.hasPlayerAndEnemy()) {
            triggerAlert("Unlucky! You have been killed by an enemy.", State.LEVEL_LOST);
        }

    }

    /**
     * Animate based on the User's change in position from the backend
     * @param userImageView The User's Image View
     * @param userCurrentCell The user's current cell
     */
    private void animatedBasedOnPosition(ImageView userImageView, Cell userCurrentCell) {
        double newX = userCurrentCell.getPosition().getX() * 64;
        double newY = userCurrentCell.getPosition().getY() * 64;
        animateUser(userImageView, newX, newY);
    }

    /**
     * Check if the user has just picked up an item
     * @param x The user's new X position
     * @param y The user's new Y position
     */
    private void checkForItemPickups(double x, double y) {
        for (ItemViewModel itemViewModel : itemViewModels) {
            double iX = itemViewModel.getImageView().getX();
            double iY = itemViewModel.getImageView().getY();

            if (iX == x && iY == y) {
                itemViewModel.getImageView().setVisible(false);
                if (itemViewModel.getItem() instanceof Key) {
                    if (!(((Key) itemViewModel.getItem()) == Key.TOKEN_KEY)) {
                        //TODO:drt - handle
                        double inventorySize = userViewModel.getUser().getInventory(currentLevel).size() - 1;
                        ImageView ig = new ImageView();
                        ig.setX(inventorySize * 64);
                        ig.setY(0);
                        ig.setImage(itemViewModel.getImage());
                        inventoryItems.getChildren().add(ig);
                    }
                } else {
                    double inventorySize = userViewModel.getUser().getInventory(currentLevel).size() - 1;
                    ImageView ig = new ImageView();
                    ig.setX(inventorySize * 64);
                    ig.setY(0);
                    ig.setImage(itemViewModel.getImage());
                    inventoryItems.getChildren().add(ig);
                }

            }
        }
    }

    /**
     * Animate a user by moving them from cell to cell in a smooth transition. Also
     * animating them walking when moving.
     * @param userImageView The user's image view
     * @param x Their new X position
     * @param y Their new Y position
     */
    private void animateUser(ImageView userImageView, double x, double y) {
        Timeline timeline = new Timeline();
        KeyValue keyValueX = new KeyValue(userImageView.xProperty(), x);
        KeyValue keyValueY = new KeyValue(userImageView.yProperty(), y);

        KeyFrame walking = new KeyFrame(Duration.millis(100), e -> {
           userImageView.setImage(new Image(ResourceRepository.getResource("User-Walk")));
        });

        KeyFrame movement = new KeyFrame(Duration.millis(500), keyValueX, keyValueY);

        timeline.setOnFinished(e -> {
            if (userViewModel.getUser().getCurrentCell() instanceof Teleporter) {
                userImageView.setVisible(true);
            }
            userImageView.setImage(new Image(ResourceRepository.getResource("User-Idle")));
            animationCompleted = true;
        });

        timeline.getKeyFrames().addAll(movement, walking);

        timeline.play();
    }

    /**
     * Move an enemy by translating their current position
     * @param deltaX The X translation value
     * @param deltaY The Y translation value
     * @param enemy The enemy image view that will be changing
     */
    private void moveEnemy(int deltaX, int deltaY, EnemyViewModel enemy) {
        ImageView enemyImageView = enemy.getImageView();
        double x =
                (clampRange(enemyImageView.getX() + deltaX,
                        0, boardPane.getWidth() - enemyImageView.getFitWidth()));

        double y =
                (clampRange(enemyImageView.getY() + deltaY,
                        0, boardPane.getHeight() - enemyImageView.getFitHeight()));

        animateEnemy(enemyImageView, x, y);

        if (enemy.getEnemy().getCurrentCell().hasPlayerAndEnemy()) {
            triggerAlert("Unlucky! You have been killed by an enemy.", State.LEVEL_LOST);
        }
    }

    /**
     * Animates an enemies movement, so in which they move smoothly from cell to cell
     * @param enemyImageView The enemy image view, that needs animated
     * @param x The enemies new X position
     * @param y The enemies new Y position
     */
    private void animateEnemy(ImageView enemyImageView, double x, double y) {
        Timeline timeline = new Timeline();
        KeyValue keyValueX = new KeyValue(enemyImageView.xProperty(), x);
        KeyValue keyValueY = new KeyValue(enemyImageView.yProperty(), y);

        KeyFrame movement = new KeyFrame(Duration.millis(500), keyValueX, keyValueY);
        timeline.getKeyFrames().add(movement);

        timeline.play();
    }

    /**
     * Clamps the range in which an entity can move in
     * @param value The potential new value
     * @param min The min value the potential value can be
     * @param max The max value the potential value can be
     * @return The final value
     */
    private double clampRange(double value, double min, double max) {
        if (value < min) return min ;
        if (value > max) return max ;
        return value ;
    }

    /**
     * Used to execute the movement phase of each enemy involved with
     * this level. Each enemy calculates the direction in which they want to
     * move in.
     */
    private void moveEnemies() {
        User user = userViewModel.getUser();
        for (EnemyViewModel evm : this.enemyViewModels) {
            Enemy enemy = evm.getEnemy();
            Direction nextDirection = enemy.getNextDirection(user, level);
            level.moveEnemy(enemy, nextDirection);

            switch (nextDirection) {
                case LEFT:
                    moveEnemy(-CELL_WIDTH, 0, evm);
                    break;
                case RIGHT:
                    moveEnemy(CELL_WIDTH, 0,evm);
                    break;
                case UP:
                    moveEnemy(0, -CELL_WIDTH, evm);
                    break;
                case DOWN:
                    moveEnemy(0, CELL_WIDTH, evm);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Used to process a key pressed, and execute specific code depending on what key
     * was pressed
     * @param code The keyboard KeyCode that was pressed
     */
    private void processKey(KeyCode code) {
        User user = userViewModel.getUser();
        try {
            if (!pressed && animationCompleted) {
                switch (code) {
                    case ESCAPE:
                        pauseGame();
                        break;
                    case LEFT:
                        level.movePlayer(user, Direction.LEFT);
                        movePlayer(-CELL_WIDTH, 0);
                        animationCompleted = false;
                        moveEnemies();
                        break;
                    case RIGHT:
                        level.movePlayer(user, Direction.RIGHT);
                        movePlayer(CELL_WIDTH, 0);
                        animationCompleted = false;
                        moveEnemies();
                        break;
                    case UP:
                        level.movePlayer(user, Direction.UP);
                        movePlayer(0, -CELL_WIDTH);
                        animationCompleted = false;
                        moveEnemies();
                        break;
                    case DOWN:
                        level.movePlayer(user, Direction.DOWN);
                        movePlayer(0, CELL_WIDTH);
                        animationCompleted = false;
                        moveEnemies();
                        break;
                    default:
                        break;
                }
            }
        } catch (InvalidMoveException ex) {
            LOGGER.log(WARNING, "The user has attempted an invalid move!", ex);
        }

    }

    /**
     * Called every timeAnimation the user wishes to pause the game
     */
    private void pauseGame() {
        timeAnimation.pause();
        Instant currentPauseTime = Instant.now();
        boardPane.setEffect(new GaussianBlur());

        VBox pauseMenu = createPauseMenu();
        Button resume = new Button("Resume");
        Button saveAndQuit = new Button("Save and Quit");
        pauseMenu.getChildren().addAll(resume, saveAndQuit);
        pauseMenu.setAlignment(Pos.BASELINE_CENTER);

        Stage popupStage = new Stage(StageStyle.TRANSPARENT);
        popupStage.initOwner(primaryStage);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setScene(new Scene(pauseMenu, Color.TRANSPARENT));

        resume.setOnAction(e -> {
            boardPane.setEffect(null);
            updateTotalPauseTime(currentPauseTime);
            popupStage.hide();
            timeAnimation.play();
        });

        saveAndQuit.setOnAction(e -> {
            popupStage.hide();
            LevelSaver.saveLevel(currentLevel, level, userViewModel.getUser(), calculateCurrentSavedTime());
            Main.getPrimaryStage().setTitle("Main Menu");
            loadMainMenu(userViewModel.getUser());
        });

        popupStage.show();
    }

    /**
     * Calculate the current elapsed timeAnimation used for saves
     * @return The calculated elapsed timeAnimation up till now
     */
    private Long calculateCurrentSavedTime() {
        Instant elapsed = Instant.now();
        Long newFinishTime =
                java.time.Duration.between(startTime, elapsed).toMillis() - totalPausedTime + totalSavedTime;
        totalPausedTime = 0L;
        totalSavedTime = 0L;

        return newFinishTime;
    }


    /**
     * Creates the style's a features for the pause menu
     * @return The Vbox holding the pause menu
     */
    private VBox createPauseMenu() {
        VBox pauseMenu = new VBox(10);
        Label header = new Label("Game Paused");
        pauseMenu.getChildren().add(header);

        pauseMenu.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8);");
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setPadding(new Insets(40));

        return pauseMenu;
    }

    /**
     * Updates the accumulated pause timeAnimation
     * @param pauseStart The timeAnimation start of the current pause
     */
    private void updateTotalPauseTime(Instant pauseStart) {
        totalPausedTime += java.time.Duration.between(pauseStart, Instant.now()).toMillis();
    }

    /**
     * Inject the required images into an Enemy View Model
     * @param enemy The enemy object
     * @return The enemies view model
     */
    private EnemyViewModel injectEnemyImages(Enemy enemy) {
        EnemyViewModel enemyViewModel = null;

        if (enemy instanceof SmartTargetingEnemy) {
            enemyViewModel = new EnemyViewModel(
                    enemy, new Image(ResourceRepository.getResource("SmartTargetingEnemy")));
        } else if (enemy instanceof DumbTargetingEnemy) {
            enemyViewModel = new EnemyViewModel(
                    enemy, new Image(ResourceRepository.getResource("DumbTargetingEnemy")));
        } else if (enemy instanceof StraightLineEnemy) {
            enemyViewModel = new EnemyViewModel(
                    enemy, new Image(ResourceRepository.getResource("StraightLineEnemy")));
        } else if (enemy instanceof WallFollowingEnemy) {
            enemyViewModel = new EnemyViewModel(
                    enemy, new Image(ResourceRepository.getResource("WallFollowingEnemy")));
        }

        return enemyViewModel;
    }


    /**
     * Used to formatted a timeAnimation from milliseconds into minutes & seconds
     * Credit - https://stackoverflow.com/questions/17624335/converting-milliseconds-to-minutes-and-seconds
     * @param time The timeAnimation to be formatted (ms)
     * @return The formatted timeAnimation
     */
    private String getFormattedTime(Long time) {
        return String.format("%d minutes, %d seconds",
                TimeUnit.MILLISECONDS.toMinutes(time),
                TimeUnit.MILLISECONDS.toSeconds(time) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
                );
    }


    /**
     * Used to add a new level finish timeAnimation to the user's score list.
     */
    private static Long addNewFinishTime() {
        User user = userViewModel.getUser();
        Instant elapsed = Instant.now();
        Long newFinishTime =
                java.time.Duration.between(startTime, elapsed).toMillis() - totalPausedTime + totalSavedTime;
        totalPausedTime = 0L;
        totalSavedTime = 0L;
        try {
            user.addQuickestTime(newFinishTime, currentLevel);
        } catch (InvalidLevelException ex) {
            LOGGER.log(WARNING, "The user has completed a level that they should not have completed", ex);
        }

        return newFinishTime;
    }

    /**
     * Set the width & height of this level
     * @param x The width of this level
     * @param y The height of this level
     */
    private void setBoardArea(int x, int y) {
        this.levelWidth = x;
        this.levelHeight = y;
    }

}

