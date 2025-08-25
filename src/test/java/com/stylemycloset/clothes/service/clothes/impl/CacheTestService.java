package com.stylemycloset.clothes.service.clothes.impl;

import static com.stylemycloset.common.config.CacheConfig.CLOTHES_CACHE;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.request.ClothesCreateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesSearchCondition;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.entity.clothes.ClothesType;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectedRepository;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.clothes.service.clothes.ClothService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;

public class CacheTestService extends IntegrationTestSupport {

  @Autowired
  private ClothService clothService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ClothesRepository clothesRepository;
  @Autowired
  private ClothesAttributeDefinitionSelectedRepository clothesAttributeSelectedValueRepository;

  @Autowired
  private CacheManager cacheManager;


  private org.springframework.cache.Cache cache() {
    org.springframework.cache.Cache cache = cacheManager.getCache(CLOTHES_CACHE);
    return cache;
  }

  @BeforeEach
  void setUp() {
    userRepository.deleteAllInBatch();
    clothesRepository.deleteAllInBatch();
    clothesAttributeSelectedValueRepository.deleteAllInBatch();
  }

  @DisplayName("트랜잭션 커밋 이후 반영")
  @Test
  void test() {
    // given
    User user = userRepository.save(new User("a", "a", "a"));
    ClothesCreateRequest createRequest = new ClothesCreateRequest(
        user.getId(),
        "옷이야",
        ClothesType.TOP.name(),
        null
    );
    ClothesDto firstCloth = clothService.createCloth(createRequest, null);
    ClothesSearchCondition condition = new ClothesSearchCondition(
        null,
        null,
        5,
        ClothesType.TOP,
        user.getId()
    );
    clothService.getClothes(condition);

    ClothDtoCursorResponse firstCached = cache().get(user.getId(), ClothDtoCursorResponse.class);

    // when : clothes 생성시 null 제약 어김
    ClothesCreateRequest createRequest2 = new ClothesCreateRequest(
        user.getId(),
        null,
        ClothesType.TOP.name(),
        null
    );
    Assertions.assertThatThrownBy(() -> clothService.createCloth(createRequest2, null))
        .isInstanceOf(DataIntegrityViolationException.class);

    // then : 두번쨰 캐시에서는 트랜잭션은 롤백 됬지만, beforeInvocation = true 설정으로 return 값을 주기전에 캐시 evict은 실행됨
    // 캐시가 트랜잭션 이후 실행되면 TransactionAwareCacheManagerProxy 필요
    // 디폴트는 캐시가 트랜잭션 이전에 실행되서 캐시 자체도 롤백되는 듯함
    ClothDtoCursorResponse secondCached = cache().get(user.getId(), ClothDtoCursorResponse.class);
    SoftAssertions.assertSoftly(softly -> {
      {
        softly.assertThat(firstCached.data())
            .extracting(ClothesDto::name)
            .containsExactly(firstCloth.name());
        softly.assertThat(secondCached.data())
            .extracting(ClothesDto::name)
            .containsExactly(firstCloth.name());
      }
    });
  }

}
