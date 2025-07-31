package com.stylemycloset.binarycontent;

import com.stylemycloset.common.entity.CreatedAtEntity;
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

  private String fileName;

  private String contentType;

  private Long size;

  public BinaryContent(String fileName, String contentType, Long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
  }

}
