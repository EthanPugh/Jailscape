package com.group18.model.entity;

import com.group18.controller.GameController;
import com.group18.exception.InvalidLevelException;
import com.group18.model.item.Collectable;
import com.group18.model.Colour;
import com.group18.model.item.ElementItem;
import com.group18.model.item.Key;

import java.io.Serializable;
import java.util.*;

/**
 * Designs the behaviours of User.
 * @author nothandotshuma
 */
public class User extends Entity {

    /**
     * Stores the name of the user.
     */
    private final String username;

    /**
     * Instantiates an arraylist to store the user's inventory.
     */
    private final Map<Integer, List<Collectable>> inventory;

    /**
     * Instantiates a hashmap to store the top 3 quickest times the user has played.
     */
    private final Map<Integer, Long[]> quickestTimes;


    /**
     * Stores the number of tokens the user gets.
     */
    private int tokens;

    /**
     * Stores the highest level achieved by the user.
     */
    private int highestLevel;

    /**
     * Constructor to create a new user.
     * @param username The username the user wants
     */
    public User(String username) {
        this.username = username;
        this.inventory = new TreeMap<>();
        this.quickestTimes = new HashMap<>();
        this.highestLevel = 1;

        inventory.put(highestLevel, new ArrayList<>());
        quickestTimes.put(highestLevel, new Long[]{0L,0L,0L});
    }

    /**
     * Gets the tokens the user has earned.
     * @return tokens
     */
    public int getTokens() {
        return tokens;
    }

    /**
     * Increments the number of tokens the user earns, each time they earn any.
     */
    public void addToken() {
        tokens++;
        GameController.setTokens();
    }

    /**
     * Returns a list of the inventory the user has earned.
     * @param level The level of the inventory we desire
     * @return inventory
     */
    public List<Collectable> getInventory(int level) {
        return inventory.get(level);
    }

    /**
     * Adds to the inventory, each time a user earns a collectable item.
     * @param level The level of the inventory we wish to add the item too
     * @param item The item that the User wants to collect
     */
    public void addItem(Collectable item, int level) {
        inventory.get(level).add(item);
    }

    /**
     * Checks if this user has a key of a specific colour
     * @param colour The colour of key
     * @param level The level of the inventory we wish to check
     * @return Boolean value suggesting if this user has a key of a specific colour
     */
    public boolean hasKey(Colour colour, int level) {
        boolean hasKey = false;
        List<Collectable> currentInv = getInventory(level);

        for (Collectable item : currentInv) {
            if (item instanceof Key) {
                hasKey = ((Key) item).getColour() == colour;
            }
        }

        return hasKey;
    }

    /**
     * Remove a key of a specific colour from the user's inventory
     * @param level The level inventory we wish to consume the key on
     * @param colour The colour of the key that needs to be consumed
     */
    public void consumeKey(Colour colour, int level) {
        List<Collectable> currentInv = getInventory(level);

        Collectable removedItem = null;
        for (Collectable item : currentInv) {
            if (item instanceof Key) {
                if (((Key) item).getColour() == colour && removedItem == null) {
                    removedItem = item;
                }
            }
        }

        currentInv.remove(removedItem);
    }

    /**
     * Checks if an item is of type Collectable.
     * @param i The item, the user needs to check upon
     * @param level The level inventory we wish to check on
     * @return boolean value depending on whether it is collectable or not.
     */
    public boolean hasItem(Class<? extends Collectable> i, int level) {
        List<Collectable> currentInv = getInventory(level);

        for (Collectable item : currentInv) {
            if (i.isInstance(item)) {
                return true;
            }
        }
       return false;
    }

    public boolean hasElementItem(ElementItem elementItem, int level) {
        List<Collectable> currentInv = getInventory(level);

        for (Collectable item : currentInv) {
            if (item == elementItem) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds the new top 3 quickest times to the hashmap.
     * @param time The new time from the level
     * @param level The level the generated time came from
     */
    public void addQuickestTime(Long time, int level) throws InvalidLevelException {
        if (level > this.highestLevel || level < 0) {
            throw new InvalidLevelException("You are trying to input a time for a level this user has not yet reached.");
        }

        Long[] levelArray = getQuickestTimesFor(level);
        if (levelArray[2] > time || levelArray[2] == 0){
            levelArray[0] = levelArray[1];
            levelArray[1] = levelArray[2];
            levelArray[2] = time;
        } else if (levelArray[1] > time || levelArray[1] == 0) {
            levelArray[0] = levelArray[1];
            levelArray[1] = time;
        } else if (levelArray[0] > time || levelArray[0] == 0) {
            levelArray[0] = time;
        }
    }

    /**
     * Returns the user's quickest times.
     * @return quickestTimes
     */
    public Map <Integer, Long[]> getAllQuickestTimes() {
        return quickestTimes;
    }

    /**
     * Gets the top 3 quickest quickest times for a level.
     * @param level The level the user wants to get the quickest times for
     * @return quickestTimes
     */
    public Long[] getQuickestTimesFor(int level) throws InvalidLevelException {
        if (level > this.highestLevel || level < 0) {
            throw new InvalidLevelException("The user has not reached this level before");
        }

        return this.quickestTimes.get(level);
    }

    /**
     * Returns the username of the user.
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Increments the level each time the user passes a stage.
     */
    public void incrementLevel() {
        quickestTimes.put(++highestLevel, new Long[]{0L,0L,0L});
        inventory.put(highestLevel, new ArrayList<>());
    }

    /**
     * Returns the highest level reached by the user.
     * @return highestLevel
     */
    public int getHighestLevel() {
        return highestLevel;
    }

    /**
     * Reset the inventory for a specific level
     * @param level The level associated with the inventory
     */
    public void resetInventory(int level) {
        inventory.replace(level, new ArrayList<>());
        tokens = 0;
    }

}
