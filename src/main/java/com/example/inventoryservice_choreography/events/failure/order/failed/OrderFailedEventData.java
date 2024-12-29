package com.example.inventoryservice_choreography.events.failure.order.failed;

import com.example.inventoryservice_choreography.model.order.OrderItemDto;

import java.util.List;

public class OrderFailedEventData {
    private String orderId;
    private Long customerId;
    private List<OrderItemDto> orderItems;

    public OrderFailedEventData(String orderId, Long customerId, List<OrderItemDto> orderItems) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderItems = orderItems;
    }

    public OrderFailedEventData() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDto> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDto> orderItems) {
        this.orderItems = orderItems;
    }
}
