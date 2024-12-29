package com.example.inventoryservice_choreography.events.failure.order.cancelled;

import com.example.inventoryservice_choreography.model.order.OrderItemDto;
import java.util.List;

public class OrderCancelledEventData {
    private Long customerId;
    private List<OrderItemDto> orderItemsDto;
    private String orderId;
    private Long deliveryAgentId;

    public OrderCancelledEventData(Long customerId, List<OrderItemDto> orderItemsDto, String orderId, Long deliveryAgentId) {
        this.customerId = customerId;
        this.orderItemsDto = orderItemsDto;
        this.orderId = orderId;
        this.deliveryAgentId = deliveryAgentId;
    }

    public OrderCancelledEventData() {
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

    public Long getDeliveryAgentId() {
        return deliveryAgentId;
    }

    public void setDeliveryAgentId(Long deliveryAgentId) {
        this.deliveryAgentId = deliveryAgentId;
    }
}
