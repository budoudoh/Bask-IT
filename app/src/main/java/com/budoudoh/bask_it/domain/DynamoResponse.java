package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/12/16.
 */
public class DynamoResponse {
    private Item Item;

    public DynamoResponse(com.budoudoh.bask_it.domain.Item item) {
        Item = item;
    }

    public com.budoudoh.bask_it.domain.Item getItem() {
        return Item;
    }

    public void setItem(com.budoudoh.bask_it.domain.Item item) {
        Item = item;
    }
}
