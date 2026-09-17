package com.akolyaev.family_tree.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonPublicResponse {

    private Long id;
    private String firstName;
    private String lastName;       // masked
    private String bio;
    private String photoUrl;
    private String birthDate;      // year only as String
    private String deathDate;      // year only as String
    private String ownerUsername;
    private Boolean isClaimed;
    private String fatherId;
    private String motherId;
    private String spouseId;
}
