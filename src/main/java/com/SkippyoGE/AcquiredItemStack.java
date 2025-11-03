package com.SkippyoGE;

public class AcquiredItemStack {

    private final int itemId;
    private int quantity;
    private final int pricePerItem;
    private final AcquisitionMethod acquisitionMethod;

    public AcquiredItemStack(int itemId, int quantity, int pricePerItem, AcquisitionMethod acquisitionMethod) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.pricePerItem = pricePerItem;
        this.acquisitionMethod = acquisitionMethod;
    }

    //Gatters
    public int getItemId() {return itemId;}
    public int getQuantity() {return quantity;}
    public int getPricePerItem() {return pricePerItem;}

    public AcquisitionMethod getAcquisitionMethod() {return acquisitionMethod;}


    //Setter for quantity
    public void setQuantity(int quantity) {this.quantity = quantity;}
}
