package com.stylemycloset.binarycontent;

import com.stylemycloset.common.entity.CreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "binary_contents")
@Where(clause = "deleted_at IS NULL")
public class BinaryContent extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "content_type")
  private String contentType;

  private Long size;

  @Column(name = "deleted_at")
  private Instant deleteAt;

  public BinaryContent(String fileName, String imageUrl, String contentType, Long size) {
    this.fileName = fileName;
    this.imageUrl = imageUrl;
    this.contentType = contentType;
    this.size = size;
  }
  
  // 원본 생성자도 유지 (호환성)
  public BinaryContent(String fileName, String contentType, Long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
  }
  
  // 파일명과 imageUrl 업데이트를 위한 setter
  public void updateFileInfo(String newFileName, String newImageUrl) {
    this.fileName = newFileName;
    this.imageUrl = newImageUrl;
  }

  // 소프트 삭제 메서드
  public void softDelete() {
    this.deleteAt = Instant.now();
  }

  // 삭제 여부 확인 메서드
  public boolean isDeleted() {
    return this.deleteAt != null;
  }

}
