package com.stylemycloset.cloth.service;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.binarycontent.BinaryContentRepository;
import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.AttributeDto;
import com.stylemycloset.cloth.entity.*;
import com.stylemycloset.cloth.repository.*;
import com.stylemycloset.testutil.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class ClothServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired private ClothService clothService;
    @Autowired private ClothRepository clothRepository;
    @Autowired private ClothingAttributeRepository clothingAttributeRepository;
    @Autowired private AttributeOptionRepository attributeOptionRepository;
    @Autowired private ClothingAttributeValueRepository clothingAttributeValueRepository;
    @Autowired private ClosetRepository closetRepository;
    @Autowired private ClothingCategoryRepository categoryRepository;
    @Autowired private BinaryContentRepository binaryContentRepository;
    @Autowired private EntityManager em;

    private Long userId;

    @BeforeEach
    void setup() {
        // 간단한 유저/옷장/카테고리 시드
        em.createNativeQuery("SET session_replication_role = 'replica'").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE clothes_to_attribute_options RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE clothes RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE clothes_categories RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE closets RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE clothes_attributes_category_options RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE clothes_attributes_categories RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE binary_contents RESTART IDENTITY CASCADE").executeUpdate();
        em.createNativeQuery("SET session_replication_role = 'origin'").executeUpdate();

        // user/closet
        com.stylemycloset.user.entity.User u = new com.stylemycloset.user.entity.User("tester","tester@example.com", com.stylemycloset.user.entity.Role.USER, com.stylemycloset.user.entity.Gender.MALE);
        // users.password NOT NULL 컬럼 대응: insert 시점에 바로 기본 비밀번호를 넣는다
        em.createNativeQuery("INSERT INTO users(id,name,email,role,locked,gender,password,created_at) VALUES (nextval('users_id_seq'),:name,:email,:role,false,:gender,:password, now())")
                .setParameter("name","tester")
                .setParameter("email","tester@example.com")
                .setParameter("role", com.stylemycloset.user.entity.Role.USER.name())
                .setParameter("gender", com.stylemycloset.user.entity.Gender.MALE.name())
                .setParameter("password", "testpass")
                .executeUpdate();
        Long insertedId = ((Number) em.createNativeQuery("select currval('users_id_seq')").getSingleResult()).longValue();
        // User 프록시 엔티티 로드하여 Closet 생성에 사용
        com.stylemycloset.user.entity.User uRef = em.find(com.stylemycloset.user.entity.User.class, insertedId);
        assertNotNull(uRef);
        Closet closet = new Closet(uRef);
        em.persist(closet);
        userId = insertedId;

        // category
        categoryRepository.save(new ClothingCategory(ClothingCategoryType.TOP));
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("의류 생성/조회/삭제 전체 흐름")
    void create_read_delete_flow() throws Exception {
        // 이미지 더미 파일(다운로드 서비스 경로를 쓰지 않고 BinaryContent 직접 생성)
        Path temp = Files.createTempFile("test-img", ".jpg");
        Files.writeString(temp, "dummy");
        BinaryContent bin = new BinaryContent(temp.toString(), "/files/images/2025/08/09/dummy.jpg", "image/jpeg", 5L);
        bin = binaryContentRepository.save(bin);

        ClothCreateRequestDto req = new ClothCreateRequestDto();
        req.setName("테스트의류");
        req.setType(ClothingCategoryType.TOP.name());
        req.setBinaryContentId(bin.getId());
        ClothResponseDto created = clothService.createCloth(req, userId);

        assertNotNull(created.getId());
        assertEquals("테스트의류", created.getName());
        assertEquals(ClothingCategoryType.TOP, created.getType());

        // 단건 조회 검증
        ClothResponseDto found = clothService.getClothResponseById(Long.valueOf(created.getId()));
        assertEquals(created.getId(), found.getId());

        // 삭제 (소프트 삭제이므로 조회 API에서는 보이지 않아야 함)
        clothService.deleteCloth(Long.valueOf(created.getId()));
        // Repository의 findById는 구현/프로바이더에 따라 @Where 적용이 보장되지 않을 수 있으므로 서비스 계층 기준으로 검증
        assertThrows(com.stylemycloset.cloth.exception.ClothesException.class,
                () -> clothService.getClothResponseById(Long.valueOf(created.getId())));
    }

    @Test
    @DisplayName("속성 업서트 - 이름/값 맵으로 저장 및 교체")
    void upsert_attributes_by_name() {
        ClothCreateRequestDto req = new ClothCreateRequestDto();
        req.setName("속성의류");
        req.setType(ClothingCategoryType.TOP.name());
        ClothResponseDto created = clothService.createCloth(req, userId);

        Long clothId = Long.valueOf(created.getId());
        clothService.upsertAttributesByName(clothId, Map.of(
                "색상", List.of("black","white"),
                "핏", List.of("regular")
        ));

        List<ClothingAttributeValue> values = clothingAttributeValueRepository.findByAttributeId(
                clothingAttributeRepository.findByName("색상").orElseThrow().getId()
        );
        assertFalse(values.isEmpty());

        // 교체
        clothService.upsertAttributesByName(clothId, Map.of(
                "색상", List.of("gray")
        ));
        List<ClothingAttributeValue> valuesAfter = clothingAttributeValueRepository.findByAttributeId(
                clothingAttributeRepository.findByName("색상").orElseThrow().getId()
        );
        // 최소 1개는 gray로 재저장됨
        assertTrue(valuesAfter.stream().anyMatch(v -> "gray".equals(v.getOption().getValue())));
    }
}


