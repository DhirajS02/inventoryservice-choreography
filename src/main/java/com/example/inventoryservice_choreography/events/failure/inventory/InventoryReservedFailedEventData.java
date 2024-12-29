package com.example.inventoryservice_choreography.events.failure.inventory;

public class InventoryReservedFailedEventData {
    private String orderId;

    public InventoryReservedFailedEventData(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
