package com.stylemycloset.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseTimeEntity {

  @Column(name = "deleted_at")
  private Instant deleteAt;

  // 소프트 삭제 메서드
  public void softDelete() {
    this.deleteAt = Instant.now();
  }

  // 삭제 여부 확인 메서드
  public boolean isDeleted() {
    return this.deleteAt != null;
  }

}
