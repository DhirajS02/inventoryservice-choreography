package com.example.inventoryservice_choreography.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class SqsConfig {

    private final String region = "us-east-1";  // Replace with your region
    private final String localstackEndpoint = "http://localhost:4566"; // LocalStack endpoint

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .endpointOverride(java.net.URI.create(localstackEndpoint))
                .credentialsProvider(DefaultCredentialsProvider.create()) // Use configured AWS credentials
                .build();
    }
}

