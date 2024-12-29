package com.example.inventoryservice_choreography.repository;

import com.example.inventoryservice_choreography.model.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, String> {
}
