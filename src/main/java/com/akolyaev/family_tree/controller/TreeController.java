package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.service.PermissionService;
import com.akolyaev.family_tree.service.PersonService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        if (authentication != null && authentication.isAuthenticated()) {
            canEdit = permissionService.canEdit(tree.getRoot().getId(), authentication.getName(), personService.getRepository());
        }
        model.addAttribute("canEditRoot", canEdit);
        return "tree";
    }
}
