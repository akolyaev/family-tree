package com.akolyaev.family_tree.service;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.repository.PersonRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public boolean isOwner(Person person, String username) {
        return person != null
                && person.getOwnerUsername() != null
                && person.getOwnerUsername().equals(username);
    }

    public boolean canEdit(Long personId, String currentUsername, PersonRepository repository, Authentication authentication) {
        return repository.findById(personId)
                .map(person -> isOwner(person, currentUsername) || hasMasterRole(authentication))
                .orElse(false);
    }

    private boolean hasMasterRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MASTER"));
    }
}
