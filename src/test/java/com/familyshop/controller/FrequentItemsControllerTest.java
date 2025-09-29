package com.familyshop.controller;

import com.familyshop.dto.FamilyDto;
import com.familyshop.dto.FrequentItemDto;
import com.familyshop.model.Family;
import com.familyshop.service.FamilyService;
import com.familyshop.service.FrequentItemService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FrequentItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class FrequentItemsControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    FamilyService familyService;

    @MockitoBean
    FrequentItemService frequentService;

    @Test
    @DisplayName("GET /api/frequent-items/{familyId} — happy: отдаёт топ частых")
    void getFrequent_ok() throws Exception {
        Family family = new Family(1L, "F");
        Mockito.when(familyService.getFamilyById(1L)).thenReturn(Optional.of(family));
        Mockito.when(frequentService.getTopItems(family))
                .thenReturn(List.of(new FrequentItemDto(1L, new FamilyDto(1L, "F", Collections.emptyList()), "Молоко", 5)));

        mvc.perform(get("/api/frequent-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itemName", is("Молоко")))
                .andExpect(jsonPath("$[0].frequency", is(5)));
    }

    @Test
    @DisplayName("POST /api/frequent-items/{familyId} — unhappy: нет параметров ⇒ 400")
    void getFrequent_badRequest() throws Exception {
        Mockito.when(familyService.getFamilyById(1L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/frequent-items/1")).andExpect(status().is4xxClientError());
    }
}
