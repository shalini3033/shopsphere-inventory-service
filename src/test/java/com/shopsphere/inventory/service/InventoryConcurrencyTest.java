
package com.shopsphere.inventory.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.shopsphere.inventory.entity.Inventory;
import com.shopsphere.inventory.repository.InventoryRepository;

@SpringBootTest
class InventoryConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void concurrentReservationsMustNotExceedAvailableStock()
            throws Exception {

        // 1. Create inventory with 5 available units
        long productId = Math.abs(
                UUID.randomUUID().getMostSignificantBits() % 1_000_000_000L
        ) + 1;

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(5);
        inventory.setReservedQuantity(0);

        inventoryService.createInventory(inventory);

        // 2. Prepare two worker threads
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            // 3. Submit two reservations of 4 units each
            Future<Boolean> first = executor.submit(
                    () -> attemptReservation(productId, ready, start));

            Future<Boolean> second = executor.submit(
                    () -> attemptReservation(productId, ready, start));

            // Wait until both workers are ready
            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both threads should become ready"
            );

            // Release both workers
            start.countDown();

            // Get each reservation result
            boolean firstSucceeded = first.get(15, TimeUnit.SECONDS);
            boolean secondSucceeded = second.get(15, TimeUnit.SECONDS);

            // 4. Exactly one reservation should succeed
            int successCount =
                    (firstSucceeded ? 1 : 0)
                    + (secondSucceeded ? 1 : 0);

            assertEquals(1, successCount);

            // 5. Check the final database state
            Inventory result = inventoryRepository
                    .findByProductId(productId)
                    .orElseThrow();

            assertEquals(1, result.getAvailableQuantity());
            assertEquals(4, result.getReservedQuantity());

        } finally {
            // Release any worker still waiting at the start gate
            start.countDown();

            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // Remove the test inventory record
            inventoryRepository.findByProductId(productId)
                    .ifPresent(inventoryRepository::delete);
        }
    }

    private boolean attemptReservation(
            Long productId,
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {

        ready.countDown();

        if (!start.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                    "Timed out waiting for the start signal");
        }

        try {
            inventoryService.reserveStock(productId, 4);
            return true;
        } catch (IllegalArgumentException ex) {
            // Expected when the other reservation uses most of the stock
            return false;
        }
    }
}
