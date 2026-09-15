package com.akolyaev.family_tree.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TreeResponse {

    private PersonResponse root;
    private PersonResponse wife;
    private List<PersonResponse> children;
}
