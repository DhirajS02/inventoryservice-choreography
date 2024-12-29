package com.example.inventoryservice_choreography.configuration;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;
import com.example.inventoryservice_choreography.events.failure.agent.AgentAssignmentFailedEvent;
import com.example.inventoryservice_choreography.events.failure.agent.AgentAssignmentFailedEventData;
import com.example.inventoryservice_choreography.events.failure.inventory.InventoryReservedFailedEvent;
import com.example.inventoryservice_choreography.events.failure.inventory.InventoryReservedFailedEventData;
import com.example.inventoryservice_choreography.events.failure.order.cancelled.OrderCancelledEvent;
import com.example.inventoryservice_choreography.events.failure.order.cancelled.OrderCancelledEventData;
import com.example.inventoryservice_choreography.events.failure.order.failed.OrderFailedEvent;
import com.example.inventoryservice_choreography.events.failure.order.failed.OrderFailedEventData;
import com.example.inventoryservice_choreography.events.success.agent.AgentAssignedEvent;
import com.example.inventoryservice_choreography.events.success.agent.AgentAssignedEventData;
import com.example.inventoryservice_choreography.events.success.inventory.InventoryReservedEvent;
import com.example.inventoryservice_choreography.events.success.inventory.InventoryReservedEventData;
import com.example.inventoryservice_choreography.events.success.order.OrderPlacedEvent;
import com.example.inventoryservice_choreography.events.success.order.OrderPlacedEventData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;


public class EventDeserializer extends JsonDeserializer<Event<?>> {
    @Override
    public Event<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);
        EventType eventType = EventType.valueOf(node.get("eventType").asText());

        return switch (eventType) {
            case ORDER_PLACED -> new OrderPlacedEvent(codec.treeToValue(node.get("data"), OrderPlacedEventData.class));
            case INVENTORY_RESERVED ->
                    new InventoryReservedEvent(codec.treeToValue(node.get("data"), InventoryReservedEventData.class));
            case AGENT_ASSIGNED ->
                    new AgentAssignedEvent(codec.treeToValue(node.get("data"), AgentAssignedEventData.class));
            case AGENT_ASSIGNMENT_FAILED ->
                new AgentAssignmentFailedEvent(codec.treeToValue(node.get("data"), AgentAssignmentFailedEventData.class));
            case ORDER_FAILED ->
                    new OrderFailedEvent(codec.treeToValue(node.get("data"), OrderFailedEventData.class));
            case ORDER_CANCELLED ->
                    new OrderCancelledEvent(codec.treeToValue(node.get("data"), OrderCancelledEventData.class));
            case INVENTORY_RESERVATION_FAILED ->
                    new InventoryReservedFailedEvent(codec.treeToValue(node.get("data"), InventoryReservedFailedEventData.class));

        };
    }
}
