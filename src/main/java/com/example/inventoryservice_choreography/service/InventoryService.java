package com.example.inventoryservice_choreography.service;

import com.example.inventoryservice_choreography.events.*;
import com.example.inventoryservice_choreography.events.failure.inventory.InventoryReservedFailedEvent;
import com.example.inventoryservice_choreography.events.failure.inventory.InventoryReservedFailedEventData;
import com.example.inventoryservice_choreography.events.failure.order.cancelled.OrderCancelledEventData;
import com.example.inventoryservice_choreography.events.failure.order.failed.OrderFailedEventData;
import com.example.inventoryservice_choreography.events.success.inventory.InventoryReservedEvent;
import com.example.inventoryservice_choreography.events.success.inventory.InventoryReservedEventData;
import com.example.inventoryservice_choreography.events.success.order.OrderPlacedEventData;
import com.example.inventoryservice_choreography.exceptions.EventParsingException;
import com.example.inventoryservice_choreography.model.ProcessedOrder;
import com.example.inventoryservice_choreography.model.order.OrderItemDto;
import com.example.inventoryservice_choreography.repository.InventoryRepository;
import com.example.inventoryservice_choreography.repository.ProcessedOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final SqsClient sqsClient;
    private final String orderQueueUrl;
    private final int maxRetries;
    private final String inventoryQueueUrl;
    private final String inventoryFailedQueueUrl;
    private final String orderFailedQueueUrl;
    private final String orderCancelledQueueUrl;
    private final ObjectMapper objectMapper;
    private final InventoryTransactionService inventoryTransactionService;
    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderRepository processedOrderRepository;


    public InventoryService(SqsClient sqsClient,
                            @Value("${queue.order.url}") String orderQueueUrl,
                            @Value("${queue.inventory.url}")String inventoryQueueUrl,
                            @Value("${queue.maxRetries}")int maxRetries,
                            @Value("${queue.inventory.failed.url}")String inventoryFailedQueueUrl,
                            @Value("${queue.order.failed.url}") String orderFailedQueueUrl,
                            @Value("${queue.inventory.order.cancelled.url}") String orderCancelledQueueUrl,
                            @Qualifier("event-deserializer") ObjectMapper objectMapper,
                            InventoryTransactionService inventoryTransactionService, InventoryRepository inventoryRepository, ProcessedOrderRepository processedOrderRepository) {
        this.sqsClient = sqsClient;
        this.orderQueueUrl = orderQueueUrl;
        this.maxRetries = maxRetries;
        this.inventoryQueueUrl = inventoryQueueUrl;
        this.inventoryFailedQueueUrl = inventoryFailedQueueUrl;
        this.orderFailedQueueUrl = orderFailedQueueUrl;
        this.orderCancelledQueueUrl = orderCancelledQueueUrl;
        this.objectMapper = objectMapper;
        this.inventoryTransactionService = inventoryTransactionService;
        this.inventoryRepository = inventoryRepository;
        this.processedOrderRepository = processedOrderRepository;
    }

    /**
     * Listens for and processes order placed events from the queue at fixed intervals.
     * This method is scheduled to run every 20 seconds with an initial delay of 20 seconds.
     *
     * @Scheduled annotation parameters:
     * - fixedRate: 20000 (executes every 20 seconds)
     * - initialDelay: 20000 (waits 20 seconds before first execution)
     *
     * Purpose:
     * - Polls the queue for new order events
     * - Books inventory for valid orders
     * - Handles inventory management and order processing
     */
    @Scheduled(fixedRate = 20000,initialDelay = 20000)
    public void onOrderPlaced(){

        logger.info("Listening to OrderPlacedEvent");

        final var receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(orderQueueUrl)
                .waitTimeSeconds(10)//Long polling, connection will wait for 10 seconds
                .maxNumberOfMessages(10)
                .build();

        final var messages = sqsClient.receiveMessage(receiveRequest).messages();
        messages.forEach(message -> {
            Event<OrderPlacedEventData> orderPlacedEvent=null;
            try {
                orderPlacedEvent =objectMapper.readValue(
                        message.body(),
                        new TypeReference<Event<OrderPlacedEventData>>() {}
                );
                logger.info("Received event: OrderPlaced with message body = {} . OrderID= {}",message.body(),orderPlacedEvent.getData().getOrderId());
                if (processedOrderRepository.existsById(orderPlacedEvent.getData().getOrderId())) {
                    logger.info("Skipping duplicate order. Order already processed OrderId: {}", orderPlacedEvent.getData().getOrderId());
                    deleteMessage(message.receiptHandle(),orderQueueUrl); // Remove from queue to prevent re-processing
                    return ; // Skip duplicate processing
                }


                // Process the event (e.g., reserving inventory, etc.)
                boolean success = processOrderPlacedEventWithRetry(orderPlacedEvent.getData(), maxRetries);

                if (success) {
                    logger.info("Successfully processed order. OrderId: {}", orderPlacedEvent.getData().getOrderId());
                    deleteMessage(message.receiptHandle(),orderQueueUrl);  // Only delete if successfully processed
                    logger.info("Successfully deleted event: OrderPlaced with message body = {} . OrderId: {}",orderPlacedEvent.getData().getOrderId(), message.body());
                    processedOrderRepository.save(new ProcessedOrder(orderPlacedEvent.getData().getOrderId()));

                }
                else {
                    // If processing fails after max retries, consider deleting the message as we have already tried
                    logger.warn("Order processing failed after {} attempts. Deleting message: {} . OrderID ={}", maxRetries, message.body(),orderPlacedEvent.getData().getOrderId());
                    deleteMessage(message.receiptHandle(),orderQueueUrl); // Remove from queue to prevent re-processing
                    logger.info("Successfully deleted event: OrderPlaced with message body = {} . OrderID={}", message.body(),orderPlacedEvent.getData().getOrderId());
                    publishInventoryReservedFailureEvent(orderPlacedEvent.getData().getOrderId());
                    processedOrderRepository.save(new ProcessedOrder(orderPlacedEvent.getData().getOrderId()));
                }

            } catch (JsonProcessingException e) {
                //Only Message processing error orders should be moved to dlq,hence delete is not invoked
                logger.error("Failed to parse OrderPlaced event",e);
               /* continue; We dont rethrow the exception otherwise other valid messages in the batch will also be halted
                But continue is commented as even without even this as its last line continue will anyway happen*/

                //throw new EventParsingException("Failed to parse OrderPlaced event: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Failed to process message: {}. Exception: {}", message.body(), e.getMessage(), e);
            }
        });
    }

//    private boolean processOrderPlacedEvent(OrderPlacedEventData orderPlaced) throws JsonProcessingException {
//        List<OrderItemDto> reservedItems = new ArrayList<>();  // Track reserved items
//
//        // Perform inventory reservation for each item
//        for (OrderItemDto item : orderPlaced.getOrderItemsDto()) {
//            boolean reserved = reserveInventory(item);
//            if (reserved) {
//                reservedItems.add(item);  // Track successfully reserved items
//            } else {
//                rollbackReservedItems(reservedItems);
//                return false;
//            }
//        }
//
//        // Publish InventoryReserved event if all items are reserved
//        publishInventoryReservedEvent(orderPlaced.getCustomerId(), orderPlaced.getOrderItemsDto());
//        return true;
//    }

    /**
     * Scheduled task that polls and processes failed order events from SQS queue.
     * Implements a long-polling mechanism to efficiently receive messages.
     *
     * Configuration:
     * - Fixed rate: 20000ms (20 seconds) between executions
     * - Initial delay: 20000ms before first execution
     * - Long polling: 10 seconds wait time for messages
     * - Batch size: Up to 10 messages per poll
     */
    @Scheduled(fixedRate = 20000,initialDelay = 20000)
    public void onOrderFailed(){
        logger.info("Listening to order failed event");
        final var receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(orderFailedQueueUrl)
                .waitTimeSeconds(10)//Long polling, connection will wait for 10 seconds
                .maxNumberOfMessages(10)
                .build();


        final var messages = sqsClient.receiveMessage(receiveRequest).messages();
        messages.forEach(message -> {
            try {
                logger.info("Received event: OrderFailed with message body = " + message.body());
                final var orderFailedEventData = objectMapper.readValue(
                        message.body(),
                        new TypeReference<Event<OrderFailedEventData>>() {}
                );
                processCompensatingTransaction(orderFailedEventData.getData().getOrderItems());
                deleteMessage(message.receiptHandle(), orderFailedQueueUrl);
                logger.info("Successfully deleted event: OrderFailed with message body = " + message.body());
            } catch (JsonProcessingException e) {
                //Only Message processing error orders shoudld be moved to dlq,hence delete is not invoked
                logger.error("Failed to parse OrderFailed event with message ID: {}. Error: {}", message.messageId(), e.getMessage(), e);
                //throw new EventParsingException("Failed to parse OrderFailed event: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Failed to process message: {}. Exception: {}", message.body(), e.getMessage(), e);
            }
        });
    }

    @Scheduled(fixedRate = 20000,initialDelay = 20000)
    public void onOrderCancelled() throws EventParsingException {
        final var receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(orderCancelledQueueUrl)
                .waitTimeSeconds(10)//Long polling, connection will wait for 10 seconds
                .maxNumberOfMessages(10)
                .build();

        logger.info("INSIDE ORDER Cancelled");

        final var messages = sqsClient.receiveMessage(receiveRequest).messages();
        messages.forEach(message -> {
            try {
                logger.info("Received event: OrderCancelled with message body = " + message.body());
                final var outerMessage = objectMapper.readValue(
                        message.body(),
                        new TypeReference<Map<String, Object>>() {}
                );

                // Extract the inner Message
                final String innerMessage = (String) outerMessage.get("Message");
                // Deserialize the inner message into your event object
                final var orderCancelledEventDataEvent = objectMapper.readValue(
                        innerMessage,
                        new TypeReference<Event<OrderCancelledEventData>>() {}
                );
                processCompensatingTransaction(orderCancelledEventDataEvent.getData().getOrderItemsDto());
                deleteMessage(message.receiptHandle(),orderCancelledQueueUrl);
                logger.info("Succesfully deleted event: ORDER Cancelled with message body = " + message.body());
            } catch (JsonProcessingException e) {
                //Only Message processing error orders shoudld be moved to dlq,hence delete is not invoked
                logger.error("Failed to parse OrderCancelled event with message ID: {}. Error: {}", message.messageId(),e.getMessage(),e);
                // throw new EventParsingException("Failed to parse OrderCancelled event: " + e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Failed to process message: {}. Exception: {}", message.body(), e.getMessage(), e);
            }
        });
    }

    /**
     * Processes an order placed event with retry mechanism.
     * Implements a retry pattern to handle transient failures during order processing.
     * Uses a bounded retry approach to prevent infinite loops and resource exhaustion.
     *
     * Retry Flow:
     * 1. Attempts to process the order
     * 2. If fails, retries up to maxRetries times
     * 3. Returns false if all retries are exhausted
     *
     * @param orderPlaced The order event data to be processed
     * @param maxRetries Maximum number of retry attempts before giving up
     * @return boolean Returns true if processing succeeds, false if all retries fail
     */
    private boolean processOrderPlacedEventWithRetry(OrderPlacedEventData orderPlaced, int maxRetries) {
        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                // Attempt to process the order
                return processOrderPlacedEvent(orderPlaced);
            } catch (Exception e) {
                if (attempt >= maxRetries) {
                    // Log the failure and send to DLQ if all retries failed
                    //We can do manual sending but  have to remove maxretry policy configurations
                    //sendMessageToDLQ(orderPlaced);
                    return false; // Indicate failure after retries
                }
                logger.info("Attempt {} failed. Retrying...", attempt);
            }
        }
        return false;
    }

    /**
     * Processes an order placed event and manages inventory reservation.
     * This method implements the core business logic for inventory management
     * in a distributed transaction using the choreography pattern.
     *
     * Flow:
     * 1. Attempts to reserve inventory for each item in the order
     * 2. Rolls back all reservations if any item fails
     * 3. Publishes success/failure events based on the outcome
     *
     * @param orderPlaced The order event data containing items to be reserved
     * @return boolean Returns true if all items were successfully reserved
     * @throws JsonProcessingException if event publishing fails
     * @throws RuntimeException if inventory reservation fails
     */
    private boolean processOrderPlacedEvent(OrderPlacedEventData orderPlaced) throws JsonProcessingException {
        logger.info("Inside processOrderPlacedEvent"+orderPlaced.getOrderId());
        final var reservedItems = new ArrayList<OrderItemDto>();
        for (OrderItemDto item : orderPlaced.getOrderItemsDto()) {
            logger.info("item"+item.getItemName());
            boolean reserved = reserveInventory(item);
            if (reserved) {
                logger.info("Item reserved"+item.getItemName());
                reservedItems.add(item);
            } else {
                logger.info("Failed for item"+item.getItemName());
                rollbackReservedItems(reservedItems);
                throw new RuntimeException("Failed to reserve inventory for item: " + item.getItemName());
            }
        }

        publishInventoryReservedEvent(orderPlaced.getCustomerId(), orderPlaced.getOrderItemsDto(),orderPlaced.getOrderId());
        return true;
    }

    /**
     * Processes a compensating transaction for failed or rolled back orders.
     * This method handles the rollback of inventory reservations when an order fails
     * or needs to be reversed in the choreography pattern.
     *
     * Flow:
     * 1. Creates a list to track items that need compensation
     * 2. Processes each order item for rollback
     * 3. Executes the actual rollback operation
     *
     * @param orderItems List of items that were previously reserved and need to be released
     * @throws JsonProcessingException if there's an error in JSON processing during event publishing
     */
    private void processCompensatingTransaction(List<OrderItemDto> orderItems) throws JsonProcessingException {
        logger.info("Inside processOrderPlacedEvent");
       final var reservedItems = new ArrayList<OrderItemDto>();

        // Loop over order items that were reserved
        for (OrderItemDto item : orderItems) {
            logger.info("Releasing item: " + item.getItemName());
            reservedItems.add(item); // Add the reserved item to the list
        }

        // Perform the rollback for all reserved items
        rollbackReservedItems(reservedItems);
    }


//    private void sendMessageToDLQ(OrderPlacedEventData orderPlaced) {
//        try {
//            // Serialize the event for DLQ
//            String eventMessageBody = objectMapper.writeValueAsString(new OrderPlacedEvent(orderPlaced));
//
//            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
//                    .queueUrl("http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/inventory-dlq") // DLQ URL
//                    .messageBody(eventMessageBody)
//                    .build();
//
//            sqsClient.sendMessage(sendMsgRequest);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Attempts to reserve inventory for a given order item.
     * This method is part of the inventory management system that handles
     * the reservation of items during the order processing flow.
     *
     * @param item The OrderItemDto containing item details and quantity to reserve
     * @return boolean Returns true if reservation is successful, false otherwise
     * @throws RuntimeException if an unexpected error occurs during reservation
     */
    public boolean reserveInventory(OrderItemDto item) {
        try {
            return inventoryTransactionService.reserveInventory(item);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Publishes an inventory reservation success event to the SQS queue.
     * This method is called after successfully reserving inventory items for an order.
     *
     * Flow:
     * 1. Creates event data with customer details, order items, and order ID
     * 2. Serializes the event to JSON format
     * 3. Publishes to Inventory SQS queue for further processing
     *
     * @param customerId The ID of the customer who placed the order
     * @param orderItemDtos List of items in the order with their quantities
     * @param orderId Unique identifier for the order
     * @throws JsonProcessingException if JSON serialization fails
     */
    private void publishInventoryReservedEvent(Long customerId, List<OrderItemDto> orderItemDtos,String orderId) throws JsonProcessingException {
        logger.info("Inside publishInventoryReservedEvent");
        final var inventoryReservedEventData=new InventoryReservedEventData(customerId,orderItemDtos,orderId);
        final var inventoryReservedEvent = new InventoryReservedEvent(inventoryReservedEventData);

        String inventoryReservedEventMessageBody = objectMapper.writeValueAsString(inventoryReservedEvent);
        logger.info("Inventory reserved event"+inventoryReservedEventMessageBody);

        final var sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(inventoryQueueUrl)
                .messageBody(inventoryReservedEventMessageBody)
                .build();

        sqsClient.sendMessage(sendMsgRequest);
    }

    /**
     * Publishes an event to notify that inventory reservation has failed for an order.
     * This method is called when the inventory cannot be reserved or when order processing fails.
     *
     * Flow:
     * 1. Creates failure event data with order ID
     * 2. Serializes event to JSON
     * 3. Publishes to Inventory failure queue
     *
     * @param orderId The ID of the order that failed inventory reservation
     * @throws JsonProcessingException if event serialization fails
     */
    private void publishInventoryReservedFailureEvent(String orderId) throws JsonProcessingException {
        logger.info("Inside publishInventoryReservedFailureEvent");
        final var inventoryReservedEventData=new InventoryReservedFailedEventData(orderId);
        final var inventoryReservedEvent = new InventoryReservedFailedEvent(inventoryReservedEventData);

        String inventoryReservedFailedEventMessageBody = objectMapper.writeValueAsString(inventoryReservedEvent);
        logger.info("Inventory reserved Failed event"+inventoryReservedFailedEventMessageBody);

        final var sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(inventoryFailedQueueUrl)
                .messageBody(inventoryReservedFailedEventMessageBody)
                .build();

        sqsClient.sendMessage(sendMsgRequest);
    }

    /**
     * Deletes a processed message from the SQS queue to prevent reprocessing.
     * In SQS, messages must be explicitly deleted after successful processing,
     * otherwise they will become visible again after the visibility timeout period.
     *
     * Process Flow:
     * 1. Message is successfully processed
     * 2. Delete request is sent to SQS using the receipt handle
     * 3. Message is permanently removed from the queue
     *
     * @param receiptHandle The receipt handle of the message to be deleted. This is a unique
     *                     identifier provided by SQS when the message is received, NOT the message ID.
     *                     Receipt handles are temporary and expire based on the visibility timeout.
     * @param queueUrl The URL of the SQS queue from which to delete the message
     *
     * @throws IllegalArgumentException if receiptHandle or queueUrl is null/empty
     */
    private void deleteMessage(String receiptHandle,String queueUrl) {
        final var deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }

    /**
     * Rolls back reserved inventory items in case of a transaction failure.
     * This method ensures inventory quantities are restored to their original state
     * when an order processing fails.
     *
     * Process:
     * 1. Iterates through each previously reserved item
     * 2. Releases (adds back) the reserved quantity to available inventory
     * 3. Logs each rollback operation for audit purposes
     *
     * @param reservedItems List of items that were previously reserved and need to be rolled back
     * @throws RuntimeException if the rollback operation fails for any item
     */
    private void rollbackReservedItems(List<OrderItemDto> reservedItems) {
        logger.info("Inside rollbackReservedItems");
        reservedItems.forEach(orderItemDto -> {
            logger.info("Rolling back reserved quantity for item: " + orderItemDto.getItemName() + " of quantity: " + orderItemDto.getQuantity());
            try {
                inventoryTransactionService.releaseInventory(orderItemDto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
