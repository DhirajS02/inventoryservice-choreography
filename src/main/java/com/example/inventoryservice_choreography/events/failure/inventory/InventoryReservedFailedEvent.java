package com.example.inventoryservice_choreography.events.failure.inventory;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;

public class InventoryReservedFailedEvent extends Event<InventoryReservedFailedEventData> {
    public InventoryReservedFailedEvent(InventoryReservedFailedEventData eventData) {
        super(EventType.INVENTORY_RESERVATION_FAILED, eventData);
    }
}
