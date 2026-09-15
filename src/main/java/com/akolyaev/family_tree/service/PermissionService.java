package com.akolyaev.family_tree.service;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.repository.PersonRepository;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public boolean isOwner(Person person, String username) {
        return person != null
                && person.getOwnerUsername() != null
                && person.getOwnerUsername().equals(username);
    }

    public boolean canEdit(Long personId, String currentUsername, PersonRepository repository) {
        return repository.findById(personId)
                .map(person -> isOwner(person, currentUsername))
                .orElse(false);
    }
}
