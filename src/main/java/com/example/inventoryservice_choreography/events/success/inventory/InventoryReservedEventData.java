package com.example.inventoryservice_choreography.events.success.inventory;

import com.example.inventoryservice_choreography.model.order.OrderItemDto;

import java.util.List;

public class InventoryReservedEventData {
    private Long customerId;
    private List<OrderItemDto> reservedItems;
    private String orderId;

    public InventoryReservedEventData(Long customerId, List<OrderItemDto> reservedItems,String orderId) {
        this.customerId = customerId;
        this.reservedItems = reservedItems;
        this.orderId=orderId;
    }

    public InventoryReservedEventData() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDto> getReservedItems() {
        return reservedItems;
    }

    public void setReservedItems(List<OrderItemDto> reservedItems) {

        this.reservedItems = reservedItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
