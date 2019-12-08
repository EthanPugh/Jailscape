package com.group18.viewmodel;

import com.group18.model.item.Collectable;
import javafx.scene.image.Image;

/**
 * A view model linking front-end assets to a specific item
 *
 * @author danielturato
 */
public class ItemViewModel extends ViewModel {

    /**
     * The collectable item associated with this view model
     */
    private Collectable item;

    /**
     * Creates a new view model
     * @param image The image associated with this view model
     * @param item The item this view-model represents
     */
    public ItemViewModel(Image image, Collectable item) {
        super(image);
        this.item = item;
    }

    /**
     * Get the item for this view model
     * @return The collectable item
     */
    public Collectable getItem() {
        return item;
    }

    /**
     * Set the item for the view model
     * @param item The new item
     */
    public void setItem(Collectable item) {
        this.item = item;
    }
}
