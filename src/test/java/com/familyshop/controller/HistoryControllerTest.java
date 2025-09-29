package com.familyshop.controller;

import com.familyshop.model.Family;
import com.familyshop.model.PurchaseHistory;
import com.familyshop.service.FamilyService;
import com.familyshop.service.PurchaseHistoryService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PurchaseHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class HistoryControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    PurchaseHistoryService historyService;
    @MockitoBean
    FamilyService familyService;

    @Test
    @DisplayName("GET /api/history — happy")
    void getHistory_ok() throws Exception {
        Family family = new Family(1L, "F");
        Mockito.when(familyService.getFamilyById(1L)).thenReturn(Optional.of(family));
        Mockito.when(historyService.getHistory(family)).thenReturn(List.of(
                new PurchaseHistory(1L, family, "Молоко", "2", LocalDateTime.now()))
        );
        mvc.perform(get("/api/history/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName", is("Молоко")))
                .andExpect(jsonPath("$[0].quantity", is("2")));
    }

    @Test
    @DisplayName("GET /api/history — unhappy: невалидный body ⇒ 400")
    void getHistory_badRequest() throws Exception {
        Mockito.when(familyService.getFamilyById(1L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/history/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
