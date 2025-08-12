package com.stylemycloset.binarycontent.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
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
public class BinaryContent extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "file_name", nullable = false)
  private String originalFileName;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "size", nullable = false)
  private Long size;


  @Column(name = "image_url")
  private String imageUrl;

  // key
  @Column(name = "object_key")
  private String objectKey;

  public BinaryContent(String originalFileName, String contentType, Long size) {
    this.originalFileName = originalFileName;
    this.contentType = contentType;
    this.size = size;
  }

  // 파일 경로를 요구할 때 objectKey 반환
  public String getFileName() {
    return this.objectKey;
  }

  public void updateFileInfo(String objectKey, String publicUrl) {
    this.objectKey = objectKey;
    this.imageUrl = publicUrl;
  }
}
