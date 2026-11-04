package com.akolyaev.family_tree.dto;

import lombok.*;

import java.util.List;

/**
 * Response for /api/auth/me endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private boolean authenticated;
    private String username;
    private List<String> roles;
}
