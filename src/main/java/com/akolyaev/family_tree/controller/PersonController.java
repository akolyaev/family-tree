package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.ClaimRequest;
import com.akolyaev.family_tree.dto.PersonPublicResponse;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.akolyaev.family_tree.dto.PhotoUploadRequest;
import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.service.PermissionService;
import com.akolyaev.family_tree.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;
    private final PermissionService permissionService;

    public PersonController(PersonService personService, PermissionService permissionService) {
        this.personService = personService;
        this.permissionService = permissionService;
    }

    // ---- CRUD ----

    @PostMapping
    public ResponseEntity<PersonPublicResponse> create(@Valid @RequestBody PersonRequest request) {
        Person person = personService.create(request);
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication authentication) {
        Person person = personService.getByIdFull(id);
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(personService.toFullResponse(person));
        }
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    @GetMapping
    public ResponseEntity<?> findAll(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(
                    personService.findAllFull().stream()
                            .map(personService::toFullResponse)
                            .collect(java.util.stream.Collectors.toList())
            );
        }
        return ResponseEntity.ok(personService.findAllPublic());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonPublicResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PersonRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        String currentUsername = authentication.getName();
        if (!permissionService.canEdit(id, currentUsername, personService.getRepository(), authentication)) {
            throw new AccessDeniedException("You can only edit your own profile");
        }
        Person person = personService.update(id, request);
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        String currentUsername = authentication.getName();
        if (!permissionService.canEdit(id, currentUsername, personService.getRepository(), authentication)) {
            throw new AccessDeniedException("You can only delete your own profile");
        }
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Tree ----

    @GetMapping("/tree")
    public ResponseEntity<TreeResponse> getFamilyTree() {
        return ResponseEntity.ok(personService.getFamilyTree());
    }

    // ---- Photo Upload ----

    @PostMapping("/{id}/photo")
    public ResponseEntity<PersonPublicResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestBody PhotoUploadRequest request) {
        Person person = personService.updatePhotoUrl(id, request.getUrl());
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    // ---- Claim Endpoint ----

    @PatchMapping("/{id}/claim")
    public ResponseEntity<PersonPublicResponse> claimPerson(
            @PathVariable Long id,
            @RequestBody ClaimRequest request) {
        Person person = personService.claimPerson(id, request.getUsername());
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }
}
