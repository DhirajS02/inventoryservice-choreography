package com.example.inventoryservice_choreography.configuration;

import com.example.inventoryservice_choreography.events.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean("event-deserializer")
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register the custom deserializer
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Event.class, new EventDeserializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
