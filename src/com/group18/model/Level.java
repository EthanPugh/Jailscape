package com.group18.model;

import com.group18.algorithm.Graph;
import com.group18.exception.InvalidDirectionException;
import com.group18.exception.InvalidMoveException;
import com.group18.model.cell.*;
import com.group18.model.entity.Enemy;
import com.group18.model.entity.Entity;
import com.group18.model.entity.User;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents each level in Jailscape.
 * Will act as a link between the backend & frontend.
 * @author danielturato
 */
public class Level {

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
     * Creates a new level
     * @param board The board for this level.
     */
    public Level(Cell[][] board) {
        this.board = board;
        this.boardHeight = board.length;
        this.boardWidth = board[0].length;
        this.graph = new Graph(this);
    }

    public Graph getGraph() {
        return graph;
    }

    public void resetGraph() {
        this.graph = new Graph(this);
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardHeight() {
        return boardHeight;
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

            // At this point, the move has been deemed valid.
            // Therefore, the newCell must be either be a ground cell or a door which can be opened.
            if (newCell instanceof Door) {
                //TODO: if door is coloured, we need to consume the key colour
                //TODO: then trigger animations/sounds registering the door is unlocked
                //TODO: then we to recalculate & re-verify the user can reach at least +1 in multiple directions
                //TODO: if they can't then they shouldn't be able to open the door
                //TODO: but if they can , then continue
                //TODO: then move the user.
            } else {
                newCell.placePlayer(user);
                user.setCurrentCell(newCell);
            }

        } else {
            throw new InvalidMoveException(String.format("Moving in a %s direction is not valid", direction));
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

    public void moveEnemy(Enemy enemy, Direction direction) {}

    public boolean isEnemyClose(User user) {
        return false;
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
     * @param row The row the cell is at
     * @param column The column the cell is at
     * @return The cell at the specific row & column
     */
    private Cell getCell(int row, int column) {
        return getCell(new Point(row, column));
    }

    private void removeCell(Point point) {}

    public void replaceCell(Point point, Cell newCell) {}

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

        // An enemy can not move on to a Wall, Goal, Element or Door cell.
        return !(cell instanceof Wall || cell instanceof Goal ||
                cell instanceof Element || cell instanceof Door);
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
     * Updates the state in the game controller
     * @param state The new state of the game
     */
    public void updateState(State state) {
        //TODO update state in game controller
    }

}
