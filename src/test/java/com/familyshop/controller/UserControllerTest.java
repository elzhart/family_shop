package com.familyshop.controller;

import com.familyshop.dto.UserDto;
import com.familyshop.model.Family;
import com.familyshop.service.FamilyService;
import com.familyshop.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    FamilyService familyService;

    @MockitoBean
    UserService userService;

    @Test
    @DisplayName("POST /api/users/register — happy path")
    void register_ok() throws Exception {
        Family family = new Family(1L, "F");
        Mockito.when(familyService.getFamilyById(1L)).thenReturn(Optional.of(family));
        Mockito.when(userService.registerUser("a@a.com", "pass123", family))
                .thenReturn(new UserDto(1L, "a@a.com", 1L));
        mvc.perform(post("/api/users/register")
                        .param("email", "a@a.com")
                        .param("password", "pass123")
                        .param("familyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("a@a.com")));
    }

    @Test
    @DisplayName("GET /api/users/{email} — unhappy: нет параметров ⇒ 400")
    void getByEmail_notFound() throws Exception {
        Mockito.when(userService.findByEmail("a@a.com")).thenReturn(Optional.empty());
        mvc.perform(get("/api/users/a@a.com")).andExpect(status().is4xxClientError());
    }
}
