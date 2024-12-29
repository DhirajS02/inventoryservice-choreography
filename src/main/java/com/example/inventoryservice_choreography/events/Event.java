package com.example.inventoryservice_choreography.events;

public abstract class Event<T>{
    private EventType eventType;
    private T data;

    public Event(EventType eventType, T data) {
        this.eventType = eventType;
        this.data = data;
    }

    public Event() {
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
