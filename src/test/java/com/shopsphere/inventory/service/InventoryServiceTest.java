package com.shopsphere.inventory.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopsphere.inventory.entity.Inventory;
import com.shopsphere.inventory.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
    }

    @Test
    void createInventory_shouldSaveInventory() {
        when(inventoryRepository.existsByProductId(101L))
                .thenReturn(false);

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.createInventory(inventory);

        assertNotNull(result);
        assertEquals(101L, result.getProductId());
        assertEquals(10, result.getAvailableQuantity());
        assertEquals(0, result.getReservedQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void createInventory_shouldRejectDuplicateProductId() {
        when(inventoryRepository.existsByProductId(101L))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.createInventory(inventory));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void createInventory_shouldRejectNegativeQuantity() {
        inventory.setAvailableQuantity(-5);

        when(inventoryRepository.existsByProductId(101L))
                .thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.createInventory(inventory));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void getInventoryByProductId_shouldReturnInventory() {
        when(inventoryRepository.findByProductId(101L))
                .thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryByProductId(101L);

        assertEquals(101L, result.getProductId());
        assertEquals(10, result.getAvailableQuantity());
    }

    @Test
    void getInventoryByProductId_shouldThrowWhenNotFound() {
        when(inventoryRepository.findByProductId(999L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.getInventoryByProductId(999L));
    }

    @Test
    void updateStock_shouldUpdateAvailableQuantity() {
    	when(inventoryRepository.findWithLockingByProductId(101L))
        .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.updateStock(101L, 25);

        assertEquals(25, result.getAvailableQuantity());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void updateStock_shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.updateStock(101L, -1));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reserveStock_shouldMoveQuantityFromAvailableToReserved() {
        when(inventoryRepository.findWithLockingByProductId(101L))
        .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Inventory result = inventoryService.reserveStock(101L, 3);

        assertEquals(7, result.getAvailableQuantity());
        assertEquals(3, result.getReservedQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveStock_shouldRejectInsufficientStock() {
    	when(inventoryRepository.findWithLockingByProductId(101L))
        .thenReturn(Optional.of(inventory));

        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.reserveStock(101L, 15));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reserveStock_shouldRejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.reserveStock(101L, 0));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reserveStock_shouldRejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> inventoryService.reserveStock(101L, -2));

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}