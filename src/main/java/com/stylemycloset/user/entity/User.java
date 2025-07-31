package com.stylemycloset.user.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.common.util.StringListJsonConverter;
import jakarta.persistence.*;
import com.stylemycloset.location.Location;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

  @Transient
  @Column(name = "linked_oauth_providers", columnDefinition = "JSON")
  @Convert(converter = StringListJsonConverter.class)
  private List<String> linkedOAuthProviders;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "location_id")
  private Location location;

}
