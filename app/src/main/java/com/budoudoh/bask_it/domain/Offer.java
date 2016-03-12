package com.budoudoh.bask_it.domain;

/**
 * Created by basilu on 3/11/16.
 */
public class Offer {

    private enum DiscountType {
        PERCENTAGE,
        FLAT
    };
    private Item item;
    private double discount;

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    private DiscountType discountType;

}
