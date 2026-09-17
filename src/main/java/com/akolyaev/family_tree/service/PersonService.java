package com.akolyaev.family_tree.service;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.PersonPublicResponse;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.exception.EntityNotFoundException;
import com.akolyaev.family_tree.repository.PersonRepository;
import com.akolyaev.family_tree.util.MaskingUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    // ---- CRUD ----

    @Transactional
    public Person create(PersonRequest request) {
        Person person = Person.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .bio(request.getBio())
                .photoUrl(request.getPhotoUrl())
                .birthDate(request.getBirthDate())
                .deathDate(request.getDeathDate())
                .ownerUsername(request.getOwnerUsername())
                .isClaimed(false)
                .build();

        if (request.getFatherId() != null) {
            personRepository.findById(request.getFatherId()).ifPresent(person::setFather);
        }
        if (request.getMotherId() != null) {
            personRepository.findById(request.getMotherId()).ifPresent(person::setMother);
        }
        if (request.getSpouseId() != null) {
            personRepository.findById(request.getSpouseId()).ifPresent(person::setSpouse);
        }

        return personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public PersonPublicResponse getByIdPublic(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        return toPublicResponse(person);
    }

    @Transactional(readOnly = true)
    public List<PersonPublicResponse> findAllPublic() {
        return personRepository.findAll().stream()
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Person update(Long id, PersonRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));

        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setBio(request.getBio());
        person.setPhotoUrl(request.getPhotoUrl());
        person.setBirthDate(request.getBirthDate());
        person.setDeathDate(request.getDeathDate());

        if (request.getFatherId() != null) {
            personRepository.findById(request.getFatherId()).ifPresent(person::setFather);
        }
        if (request.getMotherId() != null) {
            personRepository.findById(request.getMotherId()).ifPresent(person::setMother);
        }
        if (request.getSpouseId() != null) {
            personRepository.findById(request.getSpouseId()).ifPresent(person::setSpouse);
        }

        return personRepository.save(person);
    }

    @Transactional
    public void delete(Long id) {
        personRepository.deleteById(id);
    }

    // ---- Tree Logic ----

    @Transactional(readOnly = true)
    public TreeResponse getFamilyTree(String rootUsername) {
        List<Person> rootPersons = personRepository.findByOwnerUsername(rootUsername);
        if (rootPersons.isEmpty()) {
            return TreeResponse.builder().build();
        }

        Person root = rootPersons.get(0);
        PersonPublicResponse rootResponse = toPublicResponse(root);

        PersonPublicResponse wifeResponse = null;
        if (root.getSpouse() != null) {
            wifeResponse = toPublicResponse(root.getSpouse());
        }

        List<PersonPublicResponse> children = findChildrenPublic(root);

        return TreeResponse.builder()
                .root(rootResponse)
                .wife(wifeResponse)
                .children(children)
                .build();
    }

    // ---- Photo & Claim ----

    @Transactional
    public Person updatePhotoUrl(Long id, String photoUrl) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        person.setPhotoUrl(photoUrl);
        return personRepository.save(person);
    }

    @Transactional
    public Person claimPerson(Long id, String username) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        person.setOwnerUsername(username);
        person.setIsClaimed(true);
        return personRepository.save(person);
    }

    // ---- Public (masked) helpers ----

    public PersonPublicResponse toPublicResponse(Person person) {
        return PersonPublicResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(MaskingUtil.maskLastName(person.getLastName()))
                .bio(person.getBio())
                .photoUrl(person.getPhotoUrl())
                .birthDate(MaskingUtil.maskDate(person.getBirthDate()))
                .deathDate(MaskingUtil.maskDate(person.getDeathDate()))
                .ownerUsername(person.getOwnerUsername())
                .isClaimed(person.getIsClaimed())
                .fatherId(person.getFather() != null ? person.getFather().getId().toString() : null)
                .motherId(person.getMother() != null ? person.getMother().getId().toString() : null)
                .spouseId(person.getSpouse() != null ? person.getSpouse().getId().toString() : null)
                .build();
    }

    private List<PersonPublicResponse> findChildrenPublic(Person root) {
        List<Person> allPersons = personRepository.findAll();
        return allPersons.stream()
                .filter(p -> (p.getFather() != null && p.getFather().getId().equals(root.getId()))
                        || (p.getMother() != null && p.getMother().getId().equals(root.getId())))
                .map(this::toPublicResponse)
                .collect(Collectors.toList());
    }

    // ---- Accessor for PermissionService ----

    public PersonRepository getRepository() {
        return personRepository;
    }
}
