package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.BaseTimeEntity;
import com.stylemycloset.location.Location;
import jakarta.persistence.*;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
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

    public User(UserCreateRequest request) {
        this.name = request.name();
        this.email = request.email();
        this.role = Role.USER;
        this.locked = false;
    }

    public void updateRole(Role newRole) {
        if (newRole != null) {
            this.role = newRole;
        }
    }

    public void changePassword(String newPassword) {
        // 패스워드 컬럼이 아직 없으므로 no-op (확장 시 필드 추가)
    }

    public void lockUser(boolean locked) {
        this.locked = locked;
    }

    public void softDelete() {
        // Soft delete 정책이 별도 컬럼 없어서 no-op (확장 포인트)
    }

    public void updateProfile(ProfileUpdateRequest request) {
        if (request == null) return;
        if (request.name() != null) this.name = request.name();
        if (request.gender() != null) this.gender = request.gender();
        if (request.birthDate() != null) this.birthDate = request.birthDate();
        if (request.temperatureSensitivity() != null) this.temperatureSensitivity = request.temperatureSensitivity();
    }
}
