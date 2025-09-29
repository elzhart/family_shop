package com.familyshop.controller;

import com.familyshop.model.Family;
import com.familyshop.service.FamilyService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    FamilyService familyService;

    @Test
    @DisplayName("POST /api/families — happy path: создаёт семью")
    void createFamily_ok() throws Exception {
        Mockito.when(familyService.createFamily(any())).thenAnswer(inv ->
                new Family(1L, "Наша семья")
        );

        mvc.perform(post("/api/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Наша семья\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Наша семья")));
    }

    @Test
    @DisplayName("POST /api/families — unhappy: пустое имя ⇒ 400")
    void createFamily_badRequest() throws Exception {
        mvc.perform(get("/api/families/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/families/{id} — happy path")
    void getFamily_ok() throws Exception {
        Mockito.when(familyService.getFamilyById(100L)).thenReturn(Optional.of(new Family(100L, "Demo family")));
        mvc.perform(get("/api/families/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Demo family")));
    }
}
