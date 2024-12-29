package com.example.inventoryservice_choreography.events.success.agent;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;

public class AgentAssignedEvent extends Event<AgentAssignedEventData> {
    public AgentAssignedEvent(AgentAssignedEventData agentAssignedEventData) {
        super(EventType.AGENT_ASSIGNED, agentAssignedEventData);
    }
}
