package com.group18.model;

/**
 * This models a Node in a graph, that has been visited in some capacity.
 * Holds information regarding it's parent node, distance from the start node
 * and an estimated distance to the target node.
 *
 * @author danielturato nothandotshuma riyagupta
 */
public class RouteNode implements Comparable<RouteNode> {

    /**
     * The Node/Cell this RouteNode is associated with
     */
    private Node current;

    /**
     * This nodes parent
     */
    private Node parent;

    /**
     * The distance it has taken to reach this node from the starting node
     */
    private double distanceFromStart;

    /**
     * An estimated calculated final distance from the start to the target node
     * by using this node as a step in the path.
     */
    private double estimatedDistanceToTarget;

    /**
     * Create a basic RouteNode, initialising all distances to INFINITY
     * @param current The Node/Cell this RouteNode is associated with
     */
    public RouteNode(Node current) {
        this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Create a new RouteNode
     * @param current The Node/Cell this RouteNode is associated with
     * @param parent This node's parent node
     * @param distanceFromStart The distance from the starting node to this node.
     * @param estimatedDistanceToTarget The estimated distance from the start to target node
     *                                  involving this node as a step in that process
     */
    public RouteNode(Node current, Node parent, double distanceFromStart, double estimatedDistanceToTarget) {
        this.current = current;
        this.parent = parent;
        this.distanceFromStart = distanceFromStart;
        this.estimatedDistanceToTarget = estimatedDistanceToTarget;
    }

    /**
     * Comparing estimated distances to each RouteNodes. This allows finding the
     * shortest estimated distance from the start and targets nodes in a graphm
     * hence finding the shortest path.
     * @param o The RouteNode we will be comparing too
     * @return An integer value suggesting if this node has a shorter,larger or equal estimated distance
     */
    @Override
    public int compareTo(RouteNode o) {
        return Double.compare(this.estimatedDistanceToTarget, o.estimatedDistanceToTarget);
    }

    /**
     * Get the current node
     * @return The current node
     */
    public Node getCurrent() {
        return current;
    }

    /**
     * Get this node's parent node
     * @return The parent node
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Get the calculated distance from the start to this node
     * @return The distance from the start
     */
    public double getDistanceFromStart() {
        return distanceFromStart;
    }

    /**
     * Get the estimated distance from the start node to the target node
     * @return The estimated distance
     */
    public double getEstimatedDistanceToTarget() {
        return estimatedDistanceToTarget;
    }

    /**
     * Set the current node this RouteNode is associated with
     * @param current The new current node
     */
    public void setCurrent(Node current) {
        this.current = current;
    }

    /**
     * Set the parent of this node
     * @param parent The new parent
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Set the distance from the start node to this node
     * @param distanceFromStart The new distance from the start
     */
    public void setDistanceFromStart(double distanceFromStart) {
        this.distanceFromStart = distanceFromStart;
    }

    /**
     * Set the estimated distance from the start node to the target node
     * @param estimatedDistanceToTarget The new estimated distance to target
     */
    public void setEstimatedDistanceToTarget(double estimatedDistanceToTarget) {
        this.estimatedDistanceToTarget = estimatedDistanceToTarget;
    }
}
