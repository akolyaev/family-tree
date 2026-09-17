package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.dto.PersonRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_withEmptyFirstName_returns400() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("")
                .lastName("Ivanov")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.firstName").value("First name is required"));
    }

    @Test
    void create_withEmptyLastName_returns400() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("Ivan")
                .lastName("   ")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.lastName").value("Last name is required"));
    }

    @Test
    void create_withFutureBirthDate_returns400() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(LocalDate.of(2099, 1, 1))
                .build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.birthDate").value("Birth date must be in the past"));
    }

    @Test
    void create_withLongBio_returns400() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(LocalDate.of(1990, 1, 1))
                .bio("x".repeat(2001))
                .build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bio").value("Bio must not exceed 2000 characters"));
    }

    @Test
    void create_withAllValidFields_returns200() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .bio("Test bio")
                .birthDate(LocalDate.of(1990, 1, 1))
                .ownerUsername("admin")
                .build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"));
    }

    @Test
    void update_withEmptyFirstName_returns400() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("")
                .lastName("Ivanov")
                .build();

        mockMvc.perform(put("/api/persons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").value("First name is required"));
    }
}
