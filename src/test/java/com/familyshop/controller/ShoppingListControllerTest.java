package com.familyshop.controller;

import com.familyshop.dto.FamilyDto;
import com.familyshop.dto.ShoppingListDto;
import com.familyshop.model.Family;
import com.familyshop.model.ShoppingList;
import com.familyshop.service.FamilyService;
import com.familyshop.service.ShoppingListService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ShoppingListController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShoppingListControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    FamilyService familyService;

    @MockitoBean
    ShoppingListService shoppingService;

    @Test
    @DisplayName("POST /api/shopping-list — happy: создаёт пункт")
    void addItem_ok() throws Exception {
        ShoppingList saved = new ShoppingList(10L, new Family(1L, "F"), "Молоко", "2", false);
        Mockito.when(shoppingService.addItem(any())).thenReturn(saved);

        mvc.perform(post("/api/shopping-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemName\":\"Молоко\",\"quantity\":2,\"family\":{\"id\":1}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.itemName", is("Молоко")));
    }

    @Test
    @DisplayName("GET /api/shopping-list/{familyId} — happy: отдаёт список")
    void listActive_ok() throws Exception {
        List<ShoppingListDto> list = List.of(
                new ShoppingListDto(1L, new FamilyDto(1L, "F", Collections.emptyList()), "Хлеб", "1", false)
        );
        Family family = new Family(1L, "F");
        Mockito.when(familyService.getFamilyById(1L)).thenReturn(Optional.of(family));
        Mockito.when(shoppingService.getItems(family)).thenReturn(list);

        mvc.perform(get("/api/shopping-list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName", is("Хлеб")));
    }

    @Test
    @DisplayName("PUT /api/shopping-list/{id}/bought — happy: отмечает купленным")
    void markBought_ok() throws Exception {
        ShoppingListDto updated = new ShoppingListDto(1L, new FamilyDto(1L, "F", Collections.emptyList()), "Хлеб", "1", true);
        Mockito.when(shoppingService.markAsBought(1L)).thenReturn(updated);

        mvc.perform(put("/api/shopping-list/1/bought"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBought", is(true)));
    }
}
