package com.example.inventoryservice_choreography.repository;

import com.example.inventoryservice_choreography.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity - :quantity WHERE i.itemName = :itemName AND i.availableQuantity >= :quantity")
    int reserveItem(@Param("itemName") String itemName, @Param("quantity") int quantity);

    // Update query to release the reserved item by increasing the quantity back
    @Modifying
    @Query("UPDATE Inventory i SET i.availableQuantity = i.availableQuantity + :quantity WHERE i.itemName = :itemName")
    void releaseItem(@Param("itemName") String itemName, @Param("quantity") int quantity);
}

