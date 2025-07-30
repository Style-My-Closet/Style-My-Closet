package com.stylemycloset.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
public class BaseEntity {

  @CreatedDate
  @Column(updatable = false, nullable = false)
  protected Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  protected Instant updatedAt;

}
