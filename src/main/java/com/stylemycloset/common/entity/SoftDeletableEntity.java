package com.stylemycloset.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

  @Column(name = "deleted_at")
  private Instant deletedAt;

  public boolean isSoftDeleted() {
    return deletedAt != null;
  }

  public void softDelete() {
    this.deletedAt = Instant.now();
  }

  public void restore() {
    this.deletedAt = null;
  }

}
