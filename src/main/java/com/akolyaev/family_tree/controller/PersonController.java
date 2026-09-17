package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.akolyaev.family_tree.dto.PersonResponse;
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
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonRequest request) {
        Person person = personService.create(request);
        return ResponseEntity.ok(toResponse(person));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponse>> findAll() {
        return ResponseEntity.ok(personService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> update(@PathVariable Long id, @Valid @RequestBody PersonRequest request) {
        Person person = personService.update(id, request);
        return ResponseEntity.ok(toResponse(person));
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
    public ResponseEntity<PersonResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestBody String photoUrl) {
        Person person = personService.updatePhotoUrl(id, photoUrl);
        return ResponseEntity.ok(toResponse(person));
    }

    // ---- Claim Endpoint ----

    @PatchMapping("/{id}/claim")
    public ResponseEntity<PersonResponse> claimPerson(
            @PathVariable Long id,
            @RequestBody String username) {
        Person person = personService.claimPerson(id, username);
        return ResponseEntity.ok(toResponse(person));
    }

    // ---- Helper ----

    private PersonResponse toResponse(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .bio(person.getBio())
                .photoUrl(person.getPhotoUrl())
                .birthDate(person.getBirthDate())
                .deathDate(person.getDeathDate())
                .ownerUsername(person.getOwnerUsername())
                .isClaimed(person.getIsClaimed())
                .fatherId(person.getFather() != null ? person.getFather().getId().toString() : null)
                .motherId(person.getMother() != null ? person.getMother().getId().toString() : null)
                .spouseId(person.getSpouse() != null ? person.getSpouse().getId().toString() : null)
                .build();
    }
}
