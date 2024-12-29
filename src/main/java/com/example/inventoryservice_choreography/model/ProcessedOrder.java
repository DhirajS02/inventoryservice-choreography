package com.example.inventoryservice_choreography.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ProcessedOrder {
    @Id
    private String orderId;

    private LocalDateTime processedAt;

    public ProcessedOrder(String orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }
    public ProcessedOrder() {
    }

    public ProcessedOrder(String orderId, LocalDateTime processedAt) {
        this.orderId = orderId;
        this.processedAt = processedAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }


}
