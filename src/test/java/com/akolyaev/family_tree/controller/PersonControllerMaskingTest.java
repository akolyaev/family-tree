package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.akolyaev.family_tree.repository.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonControllerMaskingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Test
    void create_returnsMaskedLastNameAndYearOnlyBirthDate() throws Exception {
        PersonRequest request = PersonRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1985, 6, 15))
                .ownerUsername("admin")
                .build();

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("D**"))
                .andExpect(jsonPath("$.birthDate").value("1985"));
    }

    @Test
    void getById_returnsMaskedFields() throws Exception {
        Person saved = personRepository.save(Person.builder()
                .firstName("Maria")
                .lastName("Petrova")
                .birthDate(LocalDate.of(1990, 3, 20))
                .deathDate(LocalDate.of(2020, 7, 10))
                .ownerUsername("admin")
                .build());

        mockMvc.perform(get("/api/persons/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("P******"))
                .andExpect(jsonPath("$.birthDate").value("1990"))
                .andExpect(jsonPath("$.deathDate").value("2020"));
    }

    @Test
    void findAll_returnsMaskedFields() throws Exception {
        Person saved = personRepository.save(Person.builder()
                .firstName("Alex")
                .lastName("Ivanov")
                .birthDate(LocalDate.of(1995, 11, 5))
                .ownerUsername("admin")
                .build());

        mockMvc.perform(get("/api/persons/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("I*****"))
                .andExpect(jsonPath("$.birthDate").value("1995"));
    }

    @Test
    @WithMockUser(username = "admin")
    void update_returnsMaskedFields() throws Exception {
        Person saved = personRepository.save(Person.builder()
                .firstName("Test")
                .lastName("Testov")
                .birthDate(LocalDate.of(1980, 1, 1))
                .ownerUsername("admin")
                .build());

        PersonRequest updateRequest = PersonRequest.builder()
                .firstName("TestUpdated")
                .lastName("TestovUpdated")
                .birthDate(LocalDate.of(1980, 1, 1))
                .build();

        mockMvc.perform(put("/api/persons/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("T************"))
                .andExpect(jsonPath("$.birthDate").value("1980"));
    }
}
