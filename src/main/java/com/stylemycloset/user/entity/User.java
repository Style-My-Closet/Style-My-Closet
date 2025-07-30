package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.SoftDeleteEntity;
import com.stylemycloset.common.util.StringListJsonConverter;
import jakarta.persistence.*;
import com.stylemycloset.common.entity.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends SoftDeleteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.USER;

    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "temperature_sensitivity")
    private Integer temperatureSensitivity;

    @Column(name = "linked_oauth_providers", columnDefinition = "JSON")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> linkedOAuthProviders;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

}
