package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.dto.TreeResponse;
import com.akolyaev.family_tree.service.PersonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TreeController {

    private final PersonService personService;

    public TreeController(PersonService personService) {
        this.personService = personService;
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
    public String treePage(@RequestParam(defaultValue = "admin") String username, Model model) {
        TreeResponse tree = personService.getFamilyTree(username);
        model.addAttribute("tree", tree);
        return "tree";
    }
}
