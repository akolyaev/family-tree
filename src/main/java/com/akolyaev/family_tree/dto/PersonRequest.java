package com.akolyaev.family_tree.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;

    private String photoUrl;

    @PastOrPresent(message = "Birth date must not be in the future")
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String ownerUsername;
    private Long fatherId;
    private Long motherId;
    private Long spouseId;
}
