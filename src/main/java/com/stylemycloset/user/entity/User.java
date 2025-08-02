package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.common.util.StringListJsonConverter;
import com.stylemycloset.location.Location;
import com.stylemycloset.user.dto.UserCreateRequest;
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
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
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
}
