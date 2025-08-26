package com.stylemycloset.follow.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;


@Getter
@Entity
@Table(name = "follows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "deleted_at IS NULL")
public class Follow extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "follows_seq_gen")
  @SequenceGenerator(name = "follows_seq_gen", sequenceName = "follows_id_seq", allocationSize = 1)
  private Long id;

  @JoinColumn(name = "follower_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User follower;

  @JoinColumn(name = "followee_id", nullable = false)
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User followee;

  public Follow(User followee, User follower) {
    this.followee = followee;
    this.follower = follower;
  }

}