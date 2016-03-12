package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/12/16.
 */
public class ListItem {
    private Item item;
    private int quantity;

    public ListItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
