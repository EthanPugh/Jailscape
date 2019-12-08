package com.group18.model;

import com.group18.controller.GameController;
import com.group18.exception.InvalidLevelException;
import com.group18.exception.InvalidMoveException;
import com.group18.model.cell.*;
import com.group18.model.entity.Enemy;
import com.group18.model.entity.Entity;
import com.group18.model.entity.User;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;


/**
 * This class represents each level in Jailscape.
 * Will act as a link between the backend and the frontend.
 *
 * @author danielturato
 */
public class Level {

    /**
     * Used to log errors to the console
     */
    private static final Logger LOGGER = Logger.getLogger("Level");


    /**
     * The board for this level, holding Level.rows x Level.columns of cells.
     */
    private final Cell[][] board;

    /**
     * The width of the board
     */
    private final int boardWidth;

    /**
     * The height of the board
     */
    private final int boardHeight;

    /**
     * The graph representation for this level.
     */
    private Graph graph;

    /**
     * The level number associated with this Level
     */
    private int currentLevel;

    /**
     * Creates a new level
     * @param board The board for this level.
     * @param level The level number
     */
    public Level(Cell[][] board, int level) {
        this.board = board;
        this.boardHeight = board.length;
        this.boardWidth = board[0].length;
        this.graph = new Graph(this);
        this.currentLevel = level;
    }

    /**
     * Get the current graph associated with this Level
     * @return The graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Reset this level's graph
     */
    public void resetGraph() {
        this.graph = new Graph(this);
    }

    /**
     * Get this levels board width
     * @return The board width
     */
    public int getBoardWidth() {
        return boardWidth;
    }

    /**
     * Get this levels board height
     * @return The board height
     */
    public int getBoardHeight() {
        return boardHeight;
    }

    /**
     * Get this level's current level number
     * @return The level number
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Used to check if the entities next move will be a valid one.
     * @param entity The entity wanting to move
     * @param direction The direction the entity wants to move in
     * @return A boolean value suggesting if the move is valid.
     */
    public boolean validMove(Entity entity, Direction direction) {
        Point newPosition =
                calculateNewPosition(entity.getCurrentCell().getPosition(), direction);

        return validMoveToCell(getCell(newPosition), entity);
    }

    /**
     * Here, we move the enemy in the direction they calculated to move in.
     * There direction will always be a valid one, based on calculation.
     * @param enemy The enemy who wishes to move
     * @param direction The direction in which the enemy wishes to move in
     */
    public void moveEnemy(Enemy enemy, Direction direction) {
        Point newPosition = calculateNewPosition(enemy.getCurrentCell().getPosition(), direction);

        Cell newCell = getCell(newPosition);
        Cell oldCell = enemy.getCurrentCell();

        oldCell.removeEntity(enemy);
        try {
            newCell.placeEnemy(enemy);
            enemy.setCurrentCell(newCell);
            enemy.setDirection(direction);


        } catch (InvalidMoveException ex) {
            LOGGER.log(WARNING, "This enemy is attempting to move to an invalid cell", ex);
        }
    }

    /**
     * Here, we attempt to move a user in a direction they wish to move in
     * @param user The user who wishes to move
     * @param direction The direction in which the user wishes to move in
     * @throws InvalidMoveException If the user is unable to move in that direction, this is thrown.
     */
    public void movePlayer(User user, Direction direction) throws InvalidMoveException {
        if (validMove(user, direction)) {
            Point newPosition = calculateNewPosition(user.getCurrentCell().getPosition(), direction);

            Cell newCell = getCell(newPosition);
            Cell oldCell = user.getCurrentCell();

            oldCell.removeEntity(user);

            if (newCell instanceof Teleporter) {
                ((Teleporter) newCell).toggleAction(user);
            } else if (newCell instanceof Element) {
                Element element = (Element) newCell;
                ((Element) newCell).toggleAction(user);
                newCell.placePlayer(user);
                user.setCurrentCell(newCell);

                if (element.getElementType() == ElementType.ICE) {
                    slide(user, direction);
                }

            } else if (newCell instanceof Ground) {
                ((Ground) newCell).toggleAction(user);
                newCell.placePlayer(user);
                user.setCurrentCell(newCell);
            } else if (newCell instanceof Door) {
                Door door = (Door) newCell;

                if (door instanceof ColourDoor) {
                   ((ColourDoor) door).toggleAction(user);
                } else {
                    ((TokenDoor) door).toggleAction(user);
                }

                Ground ground = new Ground();
                replaceCell(newCell.getPosition(), ground);
                ground.placePlayer(user);
                user.setCurrentCell(ground);
                graph.resetNodes(this, new ArrayList<Point>());
            } else {
                newCell.placePlayer(user);
                user.setCurrentCell(newCell);
            }

        } else {
            GameController.playSound("PlayerBlocked");
            throw new InvalidMoveException(String.format("Moving in a %s direction is not valid", direction));
        }
    }

    private void slide(User user, Direction direction) throws InvalidMoveException {
        Point newPosition = calculateNewPosition(user.getCurrentCell().getPosition(), direction);

        Cell newCell = getCell(newPosition);
        Cell oldCell = user.getCurrentCell();

        if (newCell instanceof Element) {
            if (((Element) newCell).getElementType().equals(ElementType.ICE)) {
                oldCell.removeEntity(user);
                newCell.placePlayer(user);
                user.setCurrentCell(newCell);
                slide(user, direction);
            }
        }

    }


    /**
     * Get adjacent cells from a certain Point
     * @param point The point in which you want to find adjacent cells from
     * @return A list of adjacent cells
     */
    public List<Cell> getAdjacentCells(Point point) {
        List<Cell> adjacentCells = new ArrayList<>();
        int cellX = (int) point.getX();
        int cellY = (int) point.getY();

        if (!(cellX+1 > boardWidth)) {
            adjacentCells.add(getCell(cellY, cellX+1));
        }

        if(!(cellX-1 < 0)) {
            adjacentCells.add(getCell(cellY, cellX-1));
        }

        if (!(cellY+1 > boardHeight)) {
            adjacentCells.add(getCell(cellY+1, cellX));
        }

        if (!(cellY-1 < 0)) {
            adjacentCells.add(getCell(cellY-1, cellX));
        }

        return adjacentCells;
    }

    /**
     * Get adjacent cells from a certain cell
     * @param cell The cell in which you want to find adjacent cells from
     * @return A list of adjacent cells
     */
    public List<Cell> getAdjacentCells(Cell cell) {
        Point cellPosition = cell.getPosition();

        return getAdjacentCells(cellPosition);
    }

    /**
     * Get the whole board of cells
     * @return The board of cells in the Game.
     */
    public Cell[][] getBoard() {
        return this.board;
    }

    /**
     * Get a cell, given a pair of coordinates
     * @param point The point in which the cell is located
     * @return The cell instance at the given Point.
     */
    private Cell getCell(Point point) {
        int x = (int) point.getX();
        int y = (int) point.getY();

        return this.board[y][x];
    }

    /**
     * Get the cell at a specific point
     * @param y The y the cell is at
     * @param x The x the cell is at
     * @return The cell at the specific row & column
     */
    private Cell getCell(int y, int x) {
        return getCell(new Point(x, y));
    }

    /**
     * Used to check if an entity is able to move on to this cell.
     * @param cell The cell in which the entity wants to move on to
     * @param entity The entity that wishes to move
     * @return A boolean value, suggesting if the entity is able to move to the requested cell.
     */
    private boolean validMoveToCell(Cell cell, Entity entity) {

        if (entity instanceof User) {
            if (cell.hasPlayer()) {
                return false;
            }

            if (cell instanceof Wall) {
                if (cell instanceof Door) {
                    return ((Door) cell).canOpen(((User) entity));
                }
                return false;
            }

            return true;
        }

        boolean hasItem = false;
        if (cell instanceof Ground) {
            hasItem = ((Ground) cell).hasItem();
        }

        // An enemy can not move on to a Wall, Goal, Element or Door cell.
        return !(cell instanceof Wall || cell instanceof Goal ||
                cell instanceof Element || cell instanceof Door || hasItem);
    }

    /**
     * Calculates the position Point based on an entities previous position and the direction they wish to move
     * @param oldPosition The entities old position
     * @param direction The entities direction they wish to move in
     * @return The new Point position the entity will end up at.
     */
    private Point calculateNewPosition(Point oldPosition, Direction direction) {
        int oldX = (int) oldPosition.getX();
        int oldY = (int) oldPosition.getY();
        Point newPosition = new Point(oldX, oldY);

        switch (direction) {
            case UP:
                newPosition.translate(0,-1);
                break;
            case DOWN:
                newPosition.translate(0,1);
                break;
            case LEFT:
                newPosition.translate(-1,0);
                break;
            case RIGHT:
                newPosition.translate(1,0);
                break;
            default:
                break;
        }

        return newPosition;
    }

    /**
     * Replace the cell at a particular point
     * @param point The point of the old cell
     * @param newCell The new cell to be put on the point
     */
    private void replaceCell(Point point, Cell newCell) {
        newCell.setCoordinates(point);
        board[(int) point.getY()][(int) point.getX()] = newCell;
        GameController.replaceCell(point);
    }

}
