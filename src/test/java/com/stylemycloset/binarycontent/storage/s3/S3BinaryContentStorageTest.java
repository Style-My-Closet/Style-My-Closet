package com.stylemycloset.binarycontent.storage.s3;

import static com.stylemycloset.common.filter.LogMdcKeys.REQUEST_ID;

import com.stylemycloset.binarycontent.repository.BinaryContentRepository;
import com.stylemycloset.binarycontent.storage.s3.s3.exception.S3UploadArgumentException;
import com.stylemycloset.IntegrationTestSupport;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@TestPropertySource(properties = "style-my-closet.storage.type=s3")
class S3BinaryContentStorageTest extends IntegrationTestSupport {

  @Autowired
  private BinaryContentStorage binaryContentStorage;
  @Autowired
  private BinaryContentRepository binaryContentRepository;

  @Value("${style-my-closet.storage.s3.bucket}")
  private String bucket;

  @BeforeEach
  void setUp() {
    MDC.clear();
    binaryContentRepository.deleteAllInBatch();
  }

  @DisplayName("파일을 S3에 업로드합니다.")
  @Test
  void putAndGet() throws Exception {
    // given
    MDC.put(REQUEST_ID, "1");
    UUID id = UUID.randomUUID();
    byte[] fileBytes = "url-test".getBytes(StandardCharsets.UTF_8);

    // when
    UUID resultId = binaryContentStorage.put(id, fileBytes).get();

    // then
    InputStream inputStream = binaryContentStorage.get(resultId);
    Assertions.assertThat(inputStream.readAllBytes()).isEqualTo(fileBytes);
  }

  @DisplayName("유효하지 않은 입력이 들어오면 예외를 반환합니다.")
  @Test
  void putExceptionAndThrow() {
    // given
    UUID id = null;
    byte[] fileBytes = "url-test".getBytes(StandardCharsets.UTF_8);

    // when & then
    Assertions.assertThatThrownBy(() -> binaryContentStorage.put(id, fileBytes).get())
        .isInstanceOf(ExecutionException.class)
        .hasCauseInstanceOf(S3UploadArgumentException.class);
  }

  @DisplayName("파일에 접근할 수 있는 URL을 반환합니다.")
  @Test
  void getUrl() throws URISyntaxException {
    // given
    UUID id = UUID.randomUUID();
    byte[] fileBytes = "url-test".getBytes(StandardCharsets.UTF_8);
    binaryContentStorage.put(id, fileBytes).join();

    // when
    URL url = binaryContentStorage.getUrl(id);

    // then
    RestTemplate restTemplate = new RestTemplate();
    byte[] downloadedBytes = restTemplate.getForObject(url.toURI(), byte[].class);
    Assertions.assertThat(downloadedBytes).isEqualTo(fileBytes);
  }

}