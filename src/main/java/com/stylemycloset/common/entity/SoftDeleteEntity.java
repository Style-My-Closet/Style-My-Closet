package com.stylemycloset.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public class SoftDeleteEntity extends BaseEntity{
    @Column(name = "delete_at")
    private Instant deleteAt;
}
