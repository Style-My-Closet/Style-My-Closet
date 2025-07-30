package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {
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

//    @Column(name = "birth_date")
}
