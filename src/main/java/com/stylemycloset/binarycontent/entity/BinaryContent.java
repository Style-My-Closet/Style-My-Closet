package com.stylemycloset.binarycontent.entity;

import com.stylemycloset.common.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "binary_contents")
public class BinaryContent extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "file_name", nullable = false)
  private String originalFileName;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;

  public BinaryContent(String originalFileName, String contentType, Long size) {
    this.originalFileName = originalFileName;
    this.contentType = contentType;
    this.size = size;
  }

}
