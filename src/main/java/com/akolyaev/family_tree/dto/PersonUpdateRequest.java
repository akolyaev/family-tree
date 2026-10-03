package com.akolyaev.family_tree.dto;

import lombok.*;

import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonUpdateRequest {

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;
}
