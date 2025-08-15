package com.stylemycloset;

import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.stylemycloset.security.jwt.JwtSessionRepository;
import com.stylemycloset.binarycontent.service.ImageStoragePort;
import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import org.mockito.Mockito;


@ContextConfiguration(classes = {TestContainerSupport.TestContainersConfiguration.class})
abstract class TestContainerSupport {

  private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:16-alpine"))
      .withDatabaseName("testdb")
      .withUsername("testuser")
      .withPassword("testpass")
      .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"))
      .withCommand("postgres", "-c", "max_connections=200");

  static {
    POSTGRES_CONTAINER.start();
  }

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
    registry.add("spring.datasource.hikari.maximum-pool-size", () -> 5);
    registry.add("spring.batch.job.enabled", () -> false);
    registry.add("spring.batch.jdbc.initialize-schema", () -> "never");
  }

  @TestConfiguration
  @EnableJpaRepositories(basePackages = "com.stylemycloset")
  static class TestContainersConfiguration {
    // PostgreSQL만 사용하는 깔끔한 테스트 환경
    
    @Bean
    @Primary
    public JwtSessionRepository jwtSessionRepository() {
      return Mockito.mock(JwtSessionRepository.class);
    }
    
    @Bean
    @Primary
    public ImageStoragePort imageStoragePort() {
      return Mockito.mock(ImageStoragePort.class);
    }
    
    @Bean
    @Primary
    public BinaryContentStorage binaryContentStorage() {
      return Mockito.mock(BinaryContentStorage.class);
    }
  }

}
