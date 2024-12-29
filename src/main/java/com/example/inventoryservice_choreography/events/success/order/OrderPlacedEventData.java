package com.example.inventoryservice_choreography.events.success.order;

import com.example.inventoryservice_choreography.model.order.OrderItemDto;

import java.util.List;

public class OrderPlacedEventData {
    private Long customerId;
    private List<OrderItemDto> orderItemsDto;
    private String orderId;

    public OrderPlacedEventData(Long customerId, List<OrderItemDto> orderItemsDto, String orderId) {
        this.customerId = customerId;
        this.orderItemsDto = orderItemsDto;
        this.orderId = orderId;
    }

    public OrderPlacedEventData() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public List<OrderItemDto> getOrderItemsDto() {
        return orderItemsDto;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setOrderItemsDto(List<OrderItemDto> orderItemsDto) {
        this.orderItemsDto = orderItemsDto;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
