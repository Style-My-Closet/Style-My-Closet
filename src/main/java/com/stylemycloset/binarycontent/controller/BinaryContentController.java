package com.stylemycloset.binarycontent.controller;


import com.stylemycloset.binarycontent.dto.BinaryContentRequest;
import com.stylemycloset.binarycontent.dto.BinaryContentResult;
import com.stylemycloset.binarycontent.service.BinaryContentService;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import jakarta.validation.constraints.NotNull;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BinaryContentResult> create(
      @RequestPart(value = "multipartFile") MultipartFile file
  ) {
    BinaryContentRequest binaryContentRequest = BinaryContentRequest.from(file);
    BinaryContentResult binaryContentResult = binaryContentService.createBinaryContent(
        binaryContentRequest);

    return ResponseEntity.ok(binaryContentResult);
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentResult>> getByIdIn(
      @RequestParam(value = "binaryContentIds") List<UUID> binaryContentIds
  ) {
    List<BinaryContentResult> binaryContentResults = binaryContentService.getByIdIn(
        binaryContentIds);

    return ResponseEntity.ok(binaryContentResults);
  }

  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentResult> getById(
      @PathVariable(value = "binaryContentId") UUID binaryContentId
  ) {
    BinaryContentResult binaryContentResult = binaryContentService.getById(binaryContentId);

    return ResponseEntity.ok()
        .body(binaryContentResult);
  }

  @GetMapping("/url/{binaryContentId}")
  public ResponseEntity<String> getPresignedUrl(
      @PathVariable(value = "binaryContentId") UUID binaryContentId
  ) {
    URL url = binaryContentStorage.getUrl(binaryContentId);
    return ResponseEntity.ok().body(url.toString());
  }

  @DeleteMapping("/{binaryContentId}")
  public ResponseEntity<Void> delete(@NotNull @PathVariable UUID binaryContentId) {
    binaryContentService.delete(binaryContentId);
    return ResponseEntity.noContent().build();
  }

}