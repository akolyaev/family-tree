package com.akolyaev.family_tree.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "photo_url", length = 2000)
    private String photoUrl;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "death_date")
    private LocalDate deathDate;

    @Column(name = "owner_username")
    private String ownerUsername;

    @Column(name = "is_claimed", nullable = false)
    @Builder.Default
    private Boolean isClaimed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    @JsonBackReference("father")
    private Person father;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    @JsonBackReference("mother")
    private Person mother;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spouse_id")
    @JsonBackReference("spouse")
    private Person spouse;
}
