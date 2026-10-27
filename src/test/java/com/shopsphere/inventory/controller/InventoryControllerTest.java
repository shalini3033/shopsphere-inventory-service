
package com.shopsphere.inventory.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;
import com.shopsphere.inventory.entity.Inventory;
import com.shopsphere.inventory.service.InventoryService;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void createInventory_shouldReturnCreated() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);

        when(inventoryService.createInventory(any(Inventory.class)))
                .thenReturn(inventory);

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(101))
                .andExpect(jsonPath("$.availableQuantity").value(10));

        verify(inventoryService).createInventory(any(Inventory.class));
    }

    @Test
    void getInventory_shouldReturnInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);

        when(inventoryService.getInventoryByProductId(101L))
                .thenReturn(inventory);

        mockMvc.perform(get("/api/inventory/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(101))
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    void getAllInventory_shouldReturnList() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);

        when(inventoryService.getAllInventory())
                .thenReturn(List.of(inventory));

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(101));
    }

    @Test
    void updateStock_shouldReturnUpdatedInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(25);
        inventory.setReservedQuantity(0);

        when(inventoryService.updateStock(101L, 25))
                .thenReturn(inventory);

        mockMvc.perform(put("/api/inventory/101/stock")
                .param("quantity", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(25));
    }

    @Test
    void reserveStock_shouldReturnUpdatedInventory() throws Exception {
        Inventory inventory = new Inventory();
        inventory.setProductId(101L);
        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(3);

        when(inventoryService.reserveStock(101L, 3))
                .thenReturn(inventory);

        mockMvc.perform(post("/api/inventory/101/reserve")
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(7))
                .andExpect(jsonPath("$.reservedQuantity").value(3));
    }
}
