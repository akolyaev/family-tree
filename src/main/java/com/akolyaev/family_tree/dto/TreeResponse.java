package com.akolyaev.family_tree.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreeResponse {

    private PersonPublicResponse root;
    private PersonPublicResponse wife;
    private List<PersonPublicResponse> children;
}
