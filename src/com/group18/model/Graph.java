package com.group18.model;

import com.group18.model.cell.Cell;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A graph holding all nodes and their connections for a specific level
 * This graph will specifically be used in calculating the shortest path
 * within the game.
 *
 * @author danielturato nothandotshuma riyagupta
 */
public class Graph {

    /**
     * The set of all unique nodes in our graph
     */
    private Set<Node> nodes;

    /**
     * Holds each node's set of connections
     */
    private Map<Point, Set<Point>> connections;

    /**
     * Create a new graph based on a Level
     * @param level The level this graph will generate from
     */
    public Graph(Level level) {
        List<Point> points = new ArrayList<>();
        setNodes(level, points);
        setConnections(level, points);
    }

    /**
     * Reset the nodes for this graph
     * @param level The level associated with this graph
     * @param points The points associated with this graph
     */
    public void resetNodes(Level level, List<Point> points) {
        setNodes(level, points);
    }

    /**
     * Gets all connections from a specific node
     * @param node The node we want connections from
     * @return A set of node connections
     */
    public Set<Node> getConnections(Node node) {
        return connections.get(node.getPosition()).stream()
                .map(this::getNode)
                .collect(Collectors.toSet());
    }

    /**
     * Set all the nodes for this Graph
     * @param level The level the graph will use represent all the nodes
     * @param points All possible (x,y) points in the level
     */
    private void setNodes(Level level, List<Point> points) {
        Set<Node> nodes = new HashSet<>();
        Cell[][] cells = level.getBoard();

        for (int i = 0; i < level.getBoardHeight(); i++) {
            for (int j = 0; j < level.getBoardHeight(); j++) {
                Cell cell = cells[i][j];
                points.add(cell.getPosition());
                nodes.add(cell);
            }
        }

        this.nodes = nodes;
    }

    /**
     * This gets a Node, which holds a specific Point
     * @param point The point the node holds
     * @return The desired node
     */
    private Node getNode(Point point) {
        return nodes.stream()
                .filter(node -> node.getPosition().equals(point))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No node found with that Point"));
    }

    /**
     * Set the connections for each node in this graph
     * @param level The level this graph will represent
     * @param points All possible (x,y) points in the level
     */
    private void setConnections(Level level, List<Point> points) {
        Map<Point, Set<Point>> connections = new HashMap<>();
        int height = level.getBoardHeight();
        int width = level.getBoardWidth();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Set<Point> pointSet = new HashSet<>();

                if (i > 0) {
                    pointSet.add(points.get(((i*height) + j) - height));
                }

                if (i < height-1) {
                    pointSet.add(points.get(((i*height) + j) + height));
                }

                if (j > 0) {
                    pointSet.add(points.get(((i*height) + j) - 1));
                }

                if (j < width-1) {
                    pointSet.add(points.get(((i*height) + j) + 1));
                }

                connections.put(points.get((i*height) + j), pointSet);
            }
        }

        this.connections = connections;
    }


}
