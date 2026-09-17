package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.PersonPublicResponse;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.service.PersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    // ---- CRUD ----

    @PostMapping
    public ResponseEntity<PersonPublicResponse> create(@Valid @RequestBody PersonRequest request) {
        Person person = personService.create(request);
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonPublicResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getByIdPublic(id));
    }

    @GetMapping
    public ResponseEntity<List<PersonPublicResponse>> findAll() {
        return ResponseEntity.ok(personService.findAllPublic());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonPublicResponse> update(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
        Person person = personService.update(id, request);
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Tree ----

    @GetMapping("/tree")
    public ResponseEntity<TreeResponse> getFamilyTree(@RequestParam String username) {
        return ResponseEntity.ok(personService.getFamilyTree(username));
    }

    // ---- Photo Upload Stub ----

    @PostMapping("/{id}/photo")
    public ResponseEntity<PersonPublicResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestBody String photoUrl) {
        Person person = personService.updatePhotoUrl(id, photoUrl);
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }

    // ---- Claim Endpoint ----

    @PatchMapping("/{id}/claim")
    public ResponseEntity<PersonPublicResponse> claimPerson(
            @PathVariable Long id,
            @RequestBody String username) {
        Person person = personService.claimPerson(id, username);
        return ResponseEntity.ok(personService.toPublicResponse(person));
    }
}
