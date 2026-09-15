package com.akolyaev.family_tree.service;

import com.akolyaev.family_tree.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionServiceTest {

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService();
    }

    @Test
    void isOwner_returnsTrue_whenUsernameMatches() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        assertTrue(permissionService.isOwner(person, "admin"));
    }

    @Test
    void isOwner_returnsFalse_whenUsernameDiffers() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        assertFalse(permissionService.isOwner(person, "other"));
    }

    @Test
    void isOwner_returnsFalse_whenPersonIsNull() {
        assertFalse(permissionService.isOwner(null, "admin"));
    }

    @Test
    void isOwner_returnsFalse_whenOwnerUsernameIsNull() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername(null)
                .build();

        assertFalse(permissionService.isOwner(person, "admin"));
    }
}
