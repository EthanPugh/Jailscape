package com.group18.model.cell;

import com.group18.controller.GameController;
import com.group18.exception.InvalidMoveException;
import com.group18.model.Actionable;
import com.group18.model.entity.Entity;
import com.group18.model.entity.User;

import java.awt.*;

/**
 * Models a teleporter cell, in which users can teleport to a partner cell
 * @author danielturato nothandotshuma riyagupta
 */
public class Teleporter extends Ground implements Actionable {

    /**
     * This cells teleporter partner
     */
    private Teleporter partner;

    /**
     * Creates a new teleporter cell
     * @param partner The partner of this teleporter
     * @param point The (x,y) coordinates of this cell
     */
    public Teleporter(Teleporter partner, Point point) {
        super(point);
        this.partner = partner;
    }

    /**
     * Toggles this teleporters action, by teleporting the user to the partner cell
     * @param entity The entity in which the cell's action is toggled upon.
     */
    @Override
    public void toggleAction(Entity entity) {
        if (entity instanceof User) {
            removeEntity(entity);
            try {
                partner.placePlayer((User) entity);
                entity.setCurrentCell(partner);
                GameController.playSound("PlayerTeleport");
            } catch (InvalidMoveException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Get this teleporter's partner cell
     * @return This teleporter's partner
     */
    public Teleporter getPartner() {
        return partner;
    }

    /**
     * Set the partner teleporter of this cell
     * @param partner The new partner
     */
    public void setPartner(Teleporter partner) {
        this.partner = partner;
    }
}
