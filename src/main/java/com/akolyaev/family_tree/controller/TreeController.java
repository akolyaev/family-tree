package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.domain.Person;
import com.akolyaev.family_tree.dto.PersonRequest;
import com.akolyaev.family_tree.dto.PersonResponse;
import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.service.PermissionService;
import com.akolyaev.family_tree.service.PersonService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

import java.util.List;

@Controller
public class TreeController {

    private final PersonService personService;
    private final PermissionService permissionService;

    public TreeController(PersonService personService, PermissionService permissionService) {
        this.personService = personService;
        this.permissionService = permissionService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/tree")
    public String treePage(@RequestParam(defaultValue = "admin") String username, Authentication authentication, Model model) {
        TreeResponse tree = personService.getFamilyTree(username);
        model.addAttribute("tree", tree);
        boolean canEdit = false;
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        if (isAuthenticated) {
            canEdit = permissionService.canEdit(tree.getRoot().getId(), authentication.getName(), personService.getRepository(), authentication);
        }
        model.addAttribute("canEditRoot", canEdit);
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && tree.getRoot() != null) {
            // Pass full data for authenticated users
            model.addAttribute("fullRoot", personService.toFullResponse(
                    personService.getByIdFull(tree.getRoot().getId())));
            if (tree.getWife() != null) {
                model.addAttribute("fullWife", personService.toFullResponse(
                        personService.getByIdFull(tree.getWife().getId())));
            }
            if (tree.getChildren() != null && !tree.getChildren().isEmpty()) {
                List<PersonResponse> fullChildren = tree.getChildren().stream()
                        .map(child -> personService.toFullResponse(personService.getByIdFull(child.getId())))
                        .toList();
                model.addAttribute("fullChildren", fullChildren);
            }
        }
        return "tree";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/persons/{id}")
    public String personPage(@PathVariable Long id, Authentication authentication, Model model) {
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();
        boolean canEdit = false;
        if (isAuthenticated) {
            canEdit = permissionService.canEdit(id, authentication.getName(), personService.getRepository(), authentication);
        }
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("id", id);
        // Pass person data for the template
        Person person = personService.getByIdFull(id);
        model.addAttribute("person", person);
        if (isAuthenticated) {
            model.addAttribute("fullPerson", personService.toFullResponse(person));
        }
        return "person";
    }

    @GetMapping("/persons/{id}/edit")
    public String editPage(@PathVariable Long id, Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        String currentUsername = authentication.getName();
        if (!permissionService.canEdit(id, currentUsername, personService.getRepository(), authentication)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        Person person = personService.getByIdFull(id);
        model.addAttribute("person", person);
        model.addAttribute("request", new PersonRequest());
        return "person-edit";
    }

    @PostMapping("/persons/{id}/edit")
    public String saveEdit(
            @PathVariable Long id,
            @Valid @ModelAttribute PersonRequest request,
            Authentication authentication,
            Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        }
        String currentUsername = authentication.getName();
        if (!permissionService.canEdit(id, currentUsername, personService.getRepository(), authentication)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }
        personService.update(id, request);
        return "redirect:/persons/" + id;
    }
}
