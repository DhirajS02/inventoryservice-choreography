package com.example.inventoryservice_choreography.events.failure.order.failed;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;

public class OrderFailedEvent extends Event<OrderFailedEventData> {
    public OrderFailedEvent(OrderFailedEventData eventData) {
        super(EventType.ORDER_FAILED, eventData);
    }
}
