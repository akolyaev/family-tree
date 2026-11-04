package com.akolyaev.family_tree.controller;

import com.akolyaev.family_tree.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Authentication endpoint for SPA to check login status.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * GET /api/auth/me — check current authentication status.
     * Returns authenticated status + username + roles.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            return ResponseEntity.ok(AuthResponse.builder()
                    .authenticated(true)
                    .username(authentication.getName())
                    .roles(roles)
                    .build());
        }
        return ResponseEntity.status(401).build();
    }
}
