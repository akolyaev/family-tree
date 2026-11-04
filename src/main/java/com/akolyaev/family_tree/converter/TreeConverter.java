package com.akolyaev.family_tree.converter;

import com.akolyaev.family_tree.dto.PersonNode;
import com.akolyaev.family_tree.dto.PersonPublicResponse;
import com.akolyaev.family_tree.dto.TreeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Converts TreeResponse (root/wife/children) to PersonNode format
 * expected by Cedrine family tree library.
 *
 * TreeResponse:
 *   root (PersonPublicResponse)
 *   wife (PersonPublicResponse | null)
 *   children (List<PersonPublicResponse>)
 *
 * PersonNode (Cedrine format):
 *   person (Object) — root
 *   partner (Object) — spouse
 *   children (List<Object>)
 */
@Component
public class TreeConverter {

    /**
     * Convert TreeResponse to PersonNode for Cedrine.
     *
     * @param treeResponse the family tree from PersonService
     * @return PersonNode in Cedrine-compatible format
     */
    @SuppressWarnings("unchecked")
    public PersonNode toPersonNode(TreeResponse treeResponse) {
        if (treeResponse == null) {
            return buildEmptyNode();
        }

        PersonPublicResponse root = treeResponse.getRoot();
        if (root == null) {
            return buildEmptyNode();
        }

        List<Object> children = treeResponse.getChildren() != null
                ? (List) treeResponse.getChildren()
                : Collections.emptyList();

        return PersonNode.builder()
                .person(root)
                .partner(treeResponse.getWife())
                .children(children)
                .build();
    }

    private PersonNode buildEmptyNode() {
        return PersonNode.builder()
                .person(null)
                .partner(null)
                .children(Collections.emptyList())
                .build();
    }
}
