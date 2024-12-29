package com.example.inventoryservice_choreography.events.success.inventory;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;
public class InventoryReservedEvent extends Event<InventoryReservedEventData> {
    public InventoryReservedEvent(InventoryReservedEventData inventoryReservedData)
    {
        super(EventType.INVENTORY_RESERVED,inventoryReservedData);
    }
}
