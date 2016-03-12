package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/12/16.
 */
public class Key {
    private String sku;

    public Key(String sku) {

        this.sku = sku;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
