package com.akolyaev.family_tree.repository;

import com.akolyaev.family_tree.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findByOwnerUsername(String ownerUsername);
}
