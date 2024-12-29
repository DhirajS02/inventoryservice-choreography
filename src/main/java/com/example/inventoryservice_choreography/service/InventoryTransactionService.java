package com.example.inventoryservice_choreography.service;

import com.example.inventoryservice_choreography.model.order.OrderItemDto;
import com.example.inventoryservice_choreography.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class InventoryTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryTransactionService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryTransactionService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public boolean reserveInventory(OrderItemDto item) {
        logger.info("Is transaction active: " + TransactionSynchronizationManager.isActualTransactionActive());
        int rowsAffected = inventoryRepository.reserveItem(item.getItemName(), item.getQuantity());
        inventoryRepository.flush();
        logger.info("Rows affected: " + rowsAffected);
        return rowsAffected == 1;
    }

    @Transactional
    public void releaseInventory(OrderItemDto item) {
        inventoryRepository.releaseItem(item.getItemName(), item.getQuantity());
        inventoryRepository.flush();
    }
}
