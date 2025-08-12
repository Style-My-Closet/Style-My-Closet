package com.stylemycloset;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
 

@SpringBootTest
@ActiveProfiles("test")
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
      baseEntityManager.createNativeQuery("SET session_replication_role = 'origin'").executeUpdate();

      baseEntityManager.createNativeQuery("INSERT INTO users(id,name,email,role,locked,gender,password,created_at) VALUES (1,'tester','tester@example.com','USER',false,'MALE','testpass', now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO closets(id,user_id,created_at) VALUES (1,1, now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      baseEntityManager.createNativeQuery("INSERT INTO clothes_categories(id,name,created_at) VALUES (1,'TOP', now()) ON CONFLICT (id) DO NOTHING").executeUpdate();
      return null;
    });
  }
}
