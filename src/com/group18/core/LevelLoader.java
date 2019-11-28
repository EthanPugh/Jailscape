package com.group18.core;

import com.group18.exception.InvalidMoveException;
import com.group18.model.Colour;
import com.group18.model.Direction;
import com.group18.model.ElementType;
import com.group18.model.Level;
import com.group18.model.cell.*;
import com.group18.model.entity.*;
import com.group18.model.item.ElementItem;
import com.group18.model.item.Key;
import com.sun.javafx.util.Logging;
import javafx.scene.image.Image;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Logger;

import static com.group18.model.Direction.*;
import static java.util.logging.Level.WARNING;

/**
 * This class allows the loading of both default level files and level files
 * that have been saved in progress.
 *
 * @author danielturato
 */
public class LevelLoader {

    /**
     * The directory which will hold all the default level files
     */
    public static final String DEFAULT_LEVEL_DIRECTORY = "./src/resources/levels/Level";

    /**
     * The directory which will hold all user-saved level files
     */
    public static final String SAVED_LEVEL_DIRECTORY =  "./savedlevels/saved-level";

    /**
     * The logger which will allows us to output errors in a nice format
     */
    private static final Logger LOGGER = Logger.getLogger("LevelLoader");

    /**
     * Load a default level file for a specified level
     * @param level The level to be loaded
     * @param user The user that will be associated with the level
     * @return The level object
     */
    public static Level loadLevel(int level, User user) {
        String fileName = DEFAULT_LEVEL_DIRECTORY + level + ".txt";
        Cell[][] cells = null;

        try {
            Scanner inputStream = new Scanner(new File(fileName));
            int lineCounter = 0;
            int boardHeight = 0;
            int boardWidth = 0;

            while (inputStream.hasNextLine()) {
                Scanner line = new Scanner(inputStream.nextLine());
                line.useDelimiter(",");

                if (lineCounter == 0) {
                    cells = new Cell[line.nextInt()][line.nextInt()];

                } else {
                    Point point = new Point(boardWidth, boardHeight);
                    fillCell(line, point, cells, user);

                    if (boardWidth+1 < cells[0].length) {
                        boardWidth++;
                    } else if (boardWidth+1 >= cells[0].length) {
                        if (boardHeight+1 < cells.length) {
                            boardWidth = 0;
                            boardHeight++;
                        }
                    }
                }

                lineCounter++;
            }


            setTeleporterPartners(cells);
            Level levelObj = new Level(cells);
            setLevelFor(cells, levelObj);

            return levelObj;

        } catch (FileNotFoundException ex) {
            LOGGER.log(WARNING, String.format("Level file %s does not exist", level), ex);
        }

        return null;
    }

    /**
     * Set the level for each cell
     * @param cells The cells in the level
     * @param levelObj The level object
     */
    private static void setLevelFor(Cell[][] cells, Level levelObj) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                cells[i][j].setLevel(levelObj);
            }
        }
    }

    /**
     * There will only ever be one teleporter per level. So after all the cells are set
     * we iterate over the cells, and partner up the teleporters
     * @param cells
     */
    private static void setTeleporterPartners(Cell[][] cells) {
        Teleporter teleporter = null;
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                if (cells[i][j] instanceof Teleporter) {
                    if (teleporter == null) {
                        teleporter = (Teleporter) cells[i][j];
                    } else {
                        Teleporter newPartner = (Teleporter) cells[i][j];
                        newPartner.setPartner(teleporter);
                        teleporter.setPartner(newPartner);
                    }
                }
            }
        }
    }

    /**
     * Fill in a cell at a specific point in the level
     * @param line The current line in the level input file aka the current cell
     * @param point The point in the 2D level grid we are currently at
     * @param cells The 2D array of cells for the level
     * @param user The user associated with this level
     */
    private static void fillCell(Scanner line, Point point, Cell[][] cells, User user) {
        Cell cell = createNewCell(line, point);
        cell.setCoordinates(point);
        placeEntity(line, cell, user);
        String potentialDirection = line.next();

        if (!potentialDirection.equals("X")) {
            setDirection(cell, potentialDirection);
        }

        String potentialItem = line.next();
        if (!potentialItem.equals(("X"))) {
            setItem(cell, potentialItem);
        }

        cells[(int) point.getY()][(int) point.getX()] = cell;
    }

    /**
     * Set an item for a specified cell
     * @param cell The cell in which an item will be placed upon
     * @param potentialItem The potential item name to be placed on the cell
     */
    private static void setItem(Cell cell, String potentialItem) {
        ItemAcronym itemAcronym = ItemAcronym.valueOf(potentialItem);
        // If we can place an item, the cell must be a ground cell
        Ground ground = (Ground) cell;

        switch (itemAcronym) {
            case FBI:
                ground.setItem(ElementItem.FIRE_BOOTS);
                break;
            case FI:
                ground.setItem(ElementItem.FLIPPERS);
                break;
            case TKI:
                ground.setItem(Key.TOKEN_KEY);
                break;
            case GKI:
                ground.setItem(Key.GREEN_KEY);
                break;
            case RKI:
                ground.setItem(Key.RED_KEY);
                break;
            case BKI:
                ground.setItem(Key.BLUE_KEY);
                break;
            case YKI:
                ground.setItem(Key.YELLOW_KEY);
                break;
            default:
                break;
        }
    }

    /**
     * Set a direction for the entity currently on the specified cell
     * @param cell The cell the entity is on
     * @param potentialDirection The potential direction name
     */
    private static void setDirection(Cell cell, String potentialDirection) {
        Direction direction = retrieveDirection(potentialDirection);
        // At the start of a level, there will only be 1 entity on 1 cell
        Entity entity = cell.getCurrentEntities().get(0);
        entity.setDirection(direction);

        if (entity instanceof StraightLineEnemy) {
            ((StraightLineEnemy) entity).setOrientation(direction);
        }
    }

    /**
     * Retrieve a Direction value based on the value given in a level input file
     * @param potentialDirection The potential direction name
     * @return The direction value
     */
    private static Direction retrieveDirection(String potentialDirection) {
        Direction direction = null;

        switch (potentialDirection) {
            case "L":
                direction = LEFT;
                break;
            case "R":
                direction = RIGHT;
                break;
            case "U":
                direction = UP;
                break;
            case "D":
                direction = DOWN;
                break;
            case "I":
                direction = IDLE;
                break;
            default:
                break;
        }

        return direction;
    }

    /**
     * Place an entity on to a specified cell
     * @param line The level input line associated with the cell
     * @param cell The cell that the entity will be placed on
     * @param user The user that potentially can be placed on the cell
     */
    private static void placeEntity(Scanner line, Cell cell, User user) {
        String entityType = line.next();
        if (!entityType.equals("X")) {
            EntityAcronym entityAcronym = EntityAcronym.valueOf(entityType);
            try {
                switch (entityAcronym) {
                    case U:
                        cell.placePlayer(user);
                        user.setCurrentCell(cell);
                        user.setSpriteImage(new Image("resources/assets/Player/Idle/PlayerIdle_00.png"));
                        break;
                    case SLE:
                        StraightLineEnemy sle = new StraightLineEnemy(null);
                        sle.setSpriteImage(new Image("resources/assets/Enemy/StraightEnemyIdle.png"));
                        cell.placeEnemy(sle);
                        sle.setCurrentCell(cell);
                        break;
                    case STE:
                        SmartTargetingEnemy ste = new SmartTargetingEnemy();
                        ste.setSpriteImage(new Image("resources/assets/Enemy/SmartEnemyIdle.png"));
                        cell.placeEnemy(ste);
                        ste.setCurrentCell(cell);
                        break;
                    case WFE:
                        WallFollowingEnemy wfe = new WallFollowingEnemy();
                        wfe.setSpriteImage(new Image("resources/assets/Enemy/WallEnemyIdle.png"));
                        cell.placeEnemy(wfe);
                        wfe.setCurrentCell(cell);
                        break;
                    case DTE:
                        DumbTargetingEnemy dte = new DumbTargetingEnemy();
                        dte.setSpriteImage(new Image("resources/assets/Enemy/DumbEnemyIdle.png"));
                        cell.placeEnemy(dte);
                        dte.setCurrentCell(cell);
                        break;
                    default:
                        break;
                }
            } catch (InvalidMoveException ex) {
                LOGGER.log(WARNING, "This level file involves an entity placement that's invalid", ex);
            }
        }
    }

    /**
     * Creates a new cell
     * @param line The level input line associated with the cell
     * @param point The point in which this cell is associated with
     * @return The new generated cell
     */
    private static Cell createNewCell(Scanner line, Point point) {
        String potentialCell = line.next();
        CellAcronym cellType;
        if (potentialCell.length() > 2) {
            cellType = CellAcronym.TD;
        } else {
            cellType = CellAcronym.valueOf(potentialCell);
        }

        Cell cell = null;

        switch (cellType) {
            case WC:
                cell = new Wall(point);
                cell.setSpriteImage(new Image("resources/assets/Cell/WallCell.png"));
                break;
            case GC:
                cell = new Ground(point);
                cell.setSpriteImage(new Image("resources/assets/Cell/GroundCell.png"));
                break;
            case FC:
                cell = new Element(ElementType.FIRE, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/FireCell.png"));
                break;
            case WTC:
                cell = new Element(ElementType.WATER, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/WaterCell.png"));
                break;
            case TC:
                cell = new Teleporter(null, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/TeleporterCell.png"));
                break;
            case GOC:
                cell = new Goal(point);
                cell.setSpriteImage(new Image("resources/assets/Cell/GoalCell.png"));
                break;
            case GD:
                cell = new ColourDoor(Colour.GREEN, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/DoorGreen.png"));
                break;
            case RD:
                cell = new ColourDoor(Colour.RED, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/DoorRed.png"));
                break;
            case BD:
                cell = new ColourDoor(Colour.BLUE, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/DoorBlue.png"));
                break;
            case YD:
                cell = new ColourDoor(Colour.YELLOW, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/DoorYellow.png"));
                break;
            case TD:
                int tokens = potentialCell.charAt(3);
                cell = new TokenDoor(tokens, point);
                cell.setSpriteImage(new Image("resources/assets/Cell/TokenDoor.png"));
            default:
                break;
        }

        return cell;
    }

    /**
     * An enum representing all the cell acronyms in the level file
     */
    private static enum CellAcronym {
        WC,
        GC,
        FC,
        WTC,
        TC,
        GOC,
        TD,
        GD,
        RD,
        BD,
        YD;
    }

    /**
     * An enum representing all the entity acronyms in the level file
     */
    private static enum EntityAcronym {
        U,
        SLE,
        STE,
        WFE,
        DTE;
    }

    /**
     * An item representing all the item acronyms in the level file
     */
    private static enum ItemAcronym {
        FBI,
        FI,
        TKI,
        GKI,
        RKI,
        BKI,
        YKI;
    }

}
