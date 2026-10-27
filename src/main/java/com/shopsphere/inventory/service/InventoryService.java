
package com.shopsphere.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.inventory.entity.Inventory;
import com.shopsphere.inventory.repository.InventoryRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Inventory createInventory(Inventory inventory) {

        if (inventoryRepository.existsByProductId(inventory.getProductId())) {
            throw new IllegalArgumentException(
                    "Inventory already exists for product ID: "
                            + inventory.getProductId());
        }

        if (inventory.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Available quantity cannot be negative");
        }

        inventory.setReservedQuantity(0);

        return inventoryRepository.save(inventory);
    }

    public Inventory getInventoryByProductId(Long productId) {

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory not found for product ID: " + productId));
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Transactional
    public Inventory updateStock(Long productId, Integer quantity) {

        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative");
        }

        Inventory inventory = inventoryRepository
                .findWithLockingByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory not found for product ID: " + productId));

        inventory.setAvailableQuantity(quantity);

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory reserveStock(Long productId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be greater than zero");
        }

        Inventory inventory = inventoryRepository
                .findWithLockingByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Inventory not found for product ID: " + productId));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product ID: " + productId);
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity);

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity);

        return inventoryRepository.save(inventory);
    }
}
