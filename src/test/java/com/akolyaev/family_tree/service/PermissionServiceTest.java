package com.akolyaev.family_tree.service;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PersonRepository repository;

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

    @Test
    void canEdit_returnsTrue_whenOwner() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(person));
        Authentication auth = new TestingAuthenticationToken("admin", "password", "ROLE_USER");

        assertTrue(permissionService.canEdit(1L, "admin", repository, auth));
    }

    @Test
    void canEdit_returnsFalse_whenNotOwner() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(person));
        Authentication auth = new TestingAuthenticationToken("other", "password", "ROLE_USER");

        assertFalse(permissionService.canEdit(1L, "other", repository, auth));
    }

    @Test
    void canEdit_returnsTrue_whenMasterRole() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(person));
        Authentication auth = new TestingAuthenticationToken("other", "password", "ROLE_USER", "ROLE_MASTER");

        assertTrue(permissionService.canEdit(1L, "other", repository, auth));
    }

    @Test
    void canEdit_returnsFalse_whenPersonNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        Authentication auth = new TestingAuthenticationToken("admin", "password", "ROLE_USER");

        assertFalse(permissionService.canEdit(999L, "admin", repository, auth));
    }

    @Test
    void canEdit_returnsFalse_whenAuthenticationIsNullAndNotOwner() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(person));

        // auth == null, username != owner → false
        assertFalse(permissionService.canEdit(1L, "other", repository, null));
    }

    @Test
    void canEdit_returnsTrue_whenAuthenticationIsNullButIsOwner() {
        Person person = Person.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .ownerUsername("admin")
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(person));

        // auth == null, но username == owner → true (isOwner срабатывает)
        assertTrue(permissionService.canEdit(1L, "admin", repository, null));
    }
}
