package com.example.inventoryservice_choreography.model.order;

import java.util.List;

public class OrderRequestDTO {
    private Long customerId;
    private List<OrderItemDto> items;

    public OrderRequestDTO(Long customerId, List<OrderItemDto> items) {
        this.customerId = customerId;
        this.items = items;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}
