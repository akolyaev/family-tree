package com.akolyaev.family_tree.dto;

import lombok.*;

import java.util.List;

/**
 * PersonNode — Cedrine-compatible family tree format.
 * Represents one family unit with a root person, their partner, and children.
 * Uses Object for person/partner/children to support both full and public responses.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonNode {

    /** Root person (e.g., family head) */
    private Object person;

    /** Partner/spouse of the root person */
    private Object partner;

    /** Children of the root + partner */
    private List<Object> children;
}
