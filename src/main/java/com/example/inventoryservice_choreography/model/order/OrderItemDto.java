package com.example.inventoryservice_choreography.model.order;

public class OrderItemDto {
    private String itemName;
    private int quantity;

    public OrderItemDto(String itemName, int quantity) {
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemId(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderItemDto() {
    }
}

