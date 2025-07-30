package com.stylemycloset.cloth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.stylemycloset.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "closets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Closet {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long closetId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name="deleted_at",columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant deletedAt;


  @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;


  @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Cloth> clothes = new ArrayList<>();

}
