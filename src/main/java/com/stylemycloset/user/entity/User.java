package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.common.util.StringListJsonConverter;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import com.stylemycloset.user.dto.request.UserCreateRequest;
import com.stylemycloset.user.dto.request.UserLockUpdateRequest;
import jakarta.persistence.*;
import com.stylemycloset.location.Location;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
  @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Column(name = "locked", nullable = false)
  private boolean locked;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private Gender gender;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "temperature_sensitivity")
  private Integer temperatureSensitivity;

  @Transient
  @Convert(converter = StringListJsonConverter.class)
  private List<String> linkedOAuthProviders;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

  public User(UserCreateRequest request) {
    this.name = request.name();
    this.email = request.email();
    this.password = request.password();
    this.role = Role.USER;
    this.linkedOAuthProviders = List.of("google");
    this.locked = false;
  }

  public void updateRole(Role newRole) {
    this.role = newRole;
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
  }

  public void lockUser(boolean newLocked) {
    if (this.locked != newLocked) {
      this.locked = newLocked;
    }
  }

  public void softDelete() {
    super.setDeleteAt(Instant.now());
  }

  public void updateProfile(ProfileUpdateRequest request) {
    if (request.name() != null) {
      this.name = request.name();
    }
    if (request.gender() != null) {
      this.gender = request.gender();
    }
    if (request.birthDate() != null) {
      this.birthDate = request.birthDate();
    }
    if (request.temperatureSensitivity() != null) {
      this.temperatureSensitivity = request.temperatureSensitivity();
    }
  }

  public void setId(Long id) {// 테스트 때문에 넣었습니다.
    this.id = id;
  }


}
