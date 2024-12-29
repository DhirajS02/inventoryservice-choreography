package com.example.inventoryservice_choreography.events.success.order;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;

public class OrderPlacedEvent extends Event<OrderPlacedEventData> {
    public OrderPlacedEvent(OrderPlacedEventData orderPlacedEventData) {
        super(EventType.ORDER_PLACED, orderPlacedEventData);
    }
}
