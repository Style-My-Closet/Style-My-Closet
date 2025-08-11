package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.stylemycloset.user.entity.User;
import org.hibernate.annotations.Where;
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

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @OneToMany(mappedBy = "closet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Where(clause = "deleted_at IS NULL")
  private List<Cloth> clothes = new ArrayList<>();


  public void addCloth(Cloth cloth) {
    if (cloth == null) return;
    if (!this.clothes.contains(cloth)) {
      this.clothes.add(cloth);
    }
    if (cloth.getCloset() != this) {
      cloth.setCloset(this);
    }
  }

  public void removeCloth(Cloth cloth) {
    if (cloth == null) return;
    if (this.clothes.remove(cloth) && cloth.getCloset() == this) {
      cloth.setCloset(null);
    }
  }

  public Closet(User user) {
    this.user = user;
  }

}
