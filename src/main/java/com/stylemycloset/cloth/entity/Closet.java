package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import com.stylemycloset.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "closets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Closet extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "closets_seq_gen")
  @SequenceGenerator(name = "closets_seq_gen", sequenceName = "closets_id_seq", allocationSize = 1)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Cloth> clothes = new ArrayList<>();


  public void addCloth(Cloth cloth) {
    if (cloth == null) return;
    this.clothes.add(cloth);
    if (cloth.getCloset() != this) {
      cloth.setCloset(this);
    }
  }

  public void removeCloth(Cloth cloth) {
    if (cloth == null) return;
    this.clothes.remove(cloth);
    if (cloth.getCloset() == this) {
      cloth.setCloset(null);
    }
  }

  public Closet(User user) {
    this.userId = user != null ? user.getId() : null;
  }

  public Closet(Long userId) {
    this.userId = userId;
  }

}
