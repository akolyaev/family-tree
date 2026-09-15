package com.akolyaev.family_tree.service;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.akolyaev.family_tree.dto.PersonResponse;
import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .bio("Test bio")
                .photoUrl("http://example.com/photo.jpg")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(null)
                .ownerUsername("admin")
                .isClaimed(false)
                .build();
    }

    // ---- create ----

    @Test
    void create_returnsSavedPerson() {
        PersonRequest request = PersonRequest.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .bio("Test bio")
                .photoUrl("http://example.com/photo.jpg")
                .birthDate(LocalDate.of(1990, 1, 1))
                .ownerUsername("admin")
                .build();

        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        Person result = personService.create(request);

        assertNotNull(result);
        assertEquals("Ivan", result.getFirstName());
        verify(personRepository).save(any(Person.class));
    }

    // ---- getById ----

    @Test
    void getById_returnsPersonResponse() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));

        PersonResponse response = personService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Ivan", response.getFirstName());
        assertNull(response.getDeathDate());
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(personRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> personService.getById(99L));
    }

    // ---- findAll ----

    @Test
    void findAll_returnsAllPersons() {
        when(personRepository.findAll()).thenReturn(List.of(testPerson));

        List<PersonResponse> responses = personService.findAll();

        assertEquals(1, responses.size());
        assertEquals("Ivan", responses.get(0).getFirstName());
    }

    // ---- update ----

    @Test
    void update_updatesPersonFields() {
        PersonRequest request = PersonRequest.builder()
                .firstName("IvanUpdated")
                .lastName("IvanovUpdated")
                .bio("Updated bio")
                .build();

        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        Person result = personService.update(1L, request);

        assertEquals("IvanUpdated", result.getFirstName());
        verify(personRepository).save(testPerson);
    }

    // ---- delete ----

    @Test
    void delete_callsRepositoryDeleteById() {
        personService.delete(1L);
        verify(personRepository).deleteById(1L);
    }

    // ---- getFamilyTree ----

    @Test
    void getFamilyTree_returnsTreeWithRoot() {
        when(personRepository.findByOwnerUsername("admin")).thenReturn(List.of(testPerson));

        TreeResponse tree = personService.getFamilyTree("admin");

        assertNotNull(tree);
        assertNotNull(tree.getRoot());
        assertEquals("Ivan", tree.getRoot().getFirstName());
        assertNull(tree.getWife());
        assertNotNull(tree.getChildren());
    }

    @Test
    void getFamilyTree_returnsEmptyWhenNoRootFound() {
        when(personRepository.findByOwnerUsername("unknown")).thenReturn(List.of());

        TreeResponse tree = personService.getFamilyTree("unknown");

        assertNotNull(tree);
        assertNull(tree.getRoot());
    }

    // ---- updatePhotoUrl ----

    @Test
    void updatePhotoUrl_updatesPhotoAndReturnsPerson() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        Person result = personService.updatePhotoUrl(1L, "http://new-photo.jpg");

        assertEquals("http://new-photo.jpg", result.getPhotoUrl());
        verify(personRepository).save(testPerson);
    }

    // ---- claimPerson ----

    @Test
    void claimPerson_setsOwnerAndClaimed() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        when(personRepository.save(any(Person.class))).thenReturn(testPerson);

        Person result = personService.claimPerson(1L, "admin");

        assertEquals("admin", result.getOwnerUsername());
        assertTrue(result.getIsClaimed());
        verify(personRepository).save(testPerson);
    }
}
