package com.example.inventoryservice_choreography.events.success.agent;

import com.example.inventoryservice_choreography.model.order.OrderItemDto;

import java.util.List;

public class AgentAssignedEventData {
    private Long customerId;
    private List<OrderItemDto> orderItems;
    private Long agentId;
    private String orderId;

    public AgentAssignedEventData(Long customerId, List<OrderItemDto> orderItems, Long agentId, String orderId) {
        this.customerId = customerId;
        this.orderItems = orderItems;
        this.agentId = agentId;
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

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}

