package com.example.inventoryservice_choreography.events.failure.agent;

import com.example.inventoryservice_choreography.events.Event;
import com.example.inventoryservice_choreography.events.EventType;

public class AgentAssignmentFailedEvent extends Event<AgentAssignmentFailedEventData> {
    public AgentAssignmentFailedEvent(AgentAssignmentFailedEventData eventData) {
        super(EventType.AGENT_ASSIGNMENT_FAILED, eventData);
    }
}

