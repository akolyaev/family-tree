package com.akolyaev.family_tree.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonRequest {

    private String firstName;
    private String lastName;
    private String bio;
    private String photoUrl;
    private LocalDate birthDate;
    private LocalDate deathDate;
    private String ownerUsername;
    private Long fatherId;
    private Long motherId;
    private Long spouseId;
}
