package com.stylemycloset.testutil;

import java.util.Map;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

abstract class TestContainerSupport {

  private static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:16-alpine"))
      .withDatabaseName("testdb")
      .withUsername("testuser")
      .withPassword("testpass")
      .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"));

  static {
    POSTGRES_CONTAINER.start();
  }

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
    registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
  }

}
