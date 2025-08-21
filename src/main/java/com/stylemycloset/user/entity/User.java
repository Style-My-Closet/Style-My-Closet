package com.stylemycloset.user.entity;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.common.util.StringListJsonConverter;
import com.stylemycloset.location.Location;
import com.stylemycloset.user.dto.request.ProfileUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @Convert(converter = StringListJsonConverter.class)
  private List<String> linkedOAuthProviders;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

  private String tempPassword;

  private Instant resetPasswordTime;

  @OneToOne
  @JoinColumn(name = "profile_id")
  private BinaryContent profileImage; // 나중에 추가해주시면 감사하겠습니다.

  public User(String name, String email, String password) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.role = Role.USER;
    this.linkedOAuthProviders = new ArrayList<>(List.of());
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

  public void updateProfile(String name, Gender gender, LocalDate birthDate, Location location,
      Integer temperatureSensitivity) {
    if (name != null) {
      this.name = name;
    }
    if (gender != null) {
      this.gender = gender;
    }
    if (birthDate != null) {
      this.birthDate = birthDate;
    }
    if (location != null) {
      this.location = location;
    }
    if (temperatureSensitivity != null) {
      this.temperatureSensitivity = temperatureSensitivity;
    }
  }

  public void updateImage(BinaryContent image) {
    this.profileImage = image;
  }

  public void setId(Long id) {// 테스트 때문에 넣었습니다. // 이 부분은 제거해주세요
    this.id = id;
  }

  public void resetTempPassword(String tempPassword, Instant resetPasswordTime) {
    this.tempPassword = tempPassword;
    this.resetPasswordTime = resetPasswordTime;
  }

  public void addOAuthProvider(String providerId) {
    this.linkedOAuthProviders.add(providerId);
  }

}