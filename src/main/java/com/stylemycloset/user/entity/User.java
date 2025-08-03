package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.BaseTimeEntity;
import com.stylemycloset.location.Location;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(nullable = false)
    private Boolean locked = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "birth_date")
    private java.time.LocalDate birthDate;

    @Column(name = "temperature_sensitivity")
    private Integer temperatureSensitivity;

    @Column(name = "location_id")
    private Long locationId;

    public User(String name, String email, Role role, Gender gender) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.gender = gender;
        this.locked = false;
    }
}
