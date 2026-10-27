package com.shopsphere.inventory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.shopsphere.inventory.entity.Inventory;
import com.shopsphere.inventory.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Create inventory
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Inventory createInventory(@RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    // Get inventory by product ID
    @GetMapping("/{productId}")
    public Inventory getInventory(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId);
    }

    // Get all inventory records
    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    // Update available stock
    @PutMapping("/{productId}/stock")
    public Inventory updateStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return inventoryService.updateStock(productId, quantity);
    }

    // Reserve stock
    @PostMapping("/{productId}/reserve")
    public Inventory reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return inventoryService.reserveStock(productId, quantity);
    }
}