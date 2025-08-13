package com.stylemycloset;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Primary;
import com.stylemycloset.common.controller.GlobalControllerExceptionHandler;
import com.stylemycloset.testconfig.TestCacheConfig;
import com.stylemycloset.binarycontent.service.ImageStoragePort;
import jakarta.persistence.EntityManager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
 

@SpringBootTest
@ActiveProfiles("test")
@Import({IntegrationTestSupport.SecurityTestConfig.class,
        IntegrationTestSupport.StorageTestConfig.class,
        GlobalControllerExceptionHandler.class,
        TestCacheConfig.class})
@org.springframework.context.annotation.ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.REGEX, pattern = "com.stylemycloset.weather.*"),
                @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = com.stylemycloset.common.config.BatchConfig.class)
        }
)
public abstract class IntegrationTestSupport extends TestContainerSupport {

  @Resource
  protected EntityManager baseEntityManager;
  @Resource
  protected PlatformTransactionManager txManager;

  @BeforeEach
  protected void initBaseData() {
    if (baseEntityManager == null || txManager == null) return;

    TransactionTemplate tt = new TransactionTemplate(txManager);
    tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tt.execute(status -> {
      baseEntityManager.createNativeQuery("SET session_replication_role = 'replica'").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE comment_likes RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE feed_likes RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE feed_comments RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE feed_ootd_clothes RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE feeds RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE weather RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE clothes_to_attribute_options RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE clothes_attributes_category_options RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE clothes_attributes_categories RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE clothes RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE clothes_categories RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE closets RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE locations RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
      // 비동기 리스너로 인한 동시 잠금 회피를 위해 알림/메시지/팔로우 테이블은 순서 보장하여 개별 삭제 실행
      baseEntityManager.createNativeQuery("DELETE FROM notifications").executeUpdate();
      baseEntityManager.createNativeQuery("ALTER SEQUENCE notifications_id_seq RESTART WITH 1").executeUpdate();
      baseEntityManager.createNativeQuery("DELETE FROM messages").executeUpdate();
      baseEntityManager.createNativeQuery("ALTER SEQUENCE messages_id_seq RESTART WITH 1").executeUpdate();
      baseEntityManager.createNativeQuery("DELETE FROM follows").executeUpdate();
      baseEntityManager.createNativeQuery("ALTER SEQUENCE follows_id_seq RESTART WITH 1").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE binary_contents RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE batch_job_execution_context RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE batch_step_execution_context RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE batch_step_execution RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE batch_job_execution_params RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE batch_job_execution RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("TRUNCATE TABLE batch_job_instance RESTART IDENTITY CASCADE").executeUpdate();
      baseEntityManager.createNativeQuery("SET session_replication_role = 'origin'").executeUpdate();

      baseEntityManager.createNativeQuery("INSERT INTO users(id,name,email,role,locked,gender,password,created_at) VALUES (1,'tester','tester@example.com','ADMIN',false,'MALE','testpass', now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO users(id,name,email,role,locked,gender,password,created_at) VALUES (2,'followee','followee@example.com','ADMIN',false,'MALE','testpass', now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO users(id,name,email,role,locked,gender,password,created_at) VALUES (3,'follower','follower@example.com','ADMIN',false,'MALE','testpass', now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      // users 시퀀스를 시드 이후 다음 값으로 맞춘다(중복 키 방지)
      baseEntityManager.createNativeQuery("ALTER SEQUENCE users_id_seq RESTART WITH 4").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO closets(id,user_id,created_at) VALUES (1,1, now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO closets(id,user_id,created_at) VALUES (2,2, now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO closets(id,user_id,created_at) VALUES (3,3, now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      // closets 시퀀스도 시드 이후 다음 값으로 맞춘다
      baseEntityManager.createNativeQuery("ALTER SEQUENCE closets_id_seq RESTART WITH 4").executeUpdate();
      // 카테고리는 테스트 케이스에서 직접 생성하므로 사전 시드하지 않음 (중복 unique:name 방지)
      baseEntityManager.createNativeQuery("ALTER SEQUENCE clothes_categories_id_seq RESTART WITH 1").executeUpdate();
      return null;
    });
  }

  @TestConfiguration
  static class SecurityTestConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }
  }

  @TestConfiguration
  static class StorageTestConfig {
    @Bean
    @Primary
    ImageStoragePort imageStoragePort() {
      return new ImageStoragePort() {
        @Override
        public UploadResult upload(byte[] data, String objectKey, String contentType) {
          return new UploadResult(objectKey != null ? objectKey : "test/key.jpg",
              "https://example.com/" + (objectKey != null ? objectKey : "test/key.jpg"));
        }
        @Override
        public PresignedUrl presignPut(String objectKey, String contentType, long expirationSeconds) {
          return new PresignedUrl(objectKey, "https://example.com/presigned/" + objectKey, java.util.Map.of());
        }
        @Override
        public String publicUrl(String objectKey) {
          return "https://example.com/" + objectKey;
        }
      };
    }
  }
}
