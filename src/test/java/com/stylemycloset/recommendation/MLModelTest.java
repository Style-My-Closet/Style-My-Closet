package com.stylemycloset.recommendation;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.binarycontent.storage.s3.s3.S3BinaryContentStorage;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.mapper.ClothesMapper;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.mapper.RecommendationMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.recommendation.repository.ClothingConditionRepositoryCustom;
import com.stylemycloset.recommendation.service.MLModelService;
import com.stylemycloset.recommendation.util.ClothingConditionBuilderHelper;
import com.stylemycloset.recommendation.util.ConditionVectorizer;
import com.stylemycloset.recommendation.util.MeaningfulDummyGenerator;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MLModelTest {

  @Mock
  private ClothingConditionRepository clothingConditionRepository;

  @Mock
  private ClothingConditionRepositoryCustom clothingConditionRepositoryCustom;

  private final ConditionVectorizer conditionVectorizer = new ConditionVectorizer();

  private final ClothingConditionBuilderHelper clothingConditionBuilderHelper = new ClothingConditionBuilderHelper();

  private final ClothingConditionMapper clothingConditionMapper = new ClothingConditionMapper(
      conditionVectorizer, clothingConditionBuilderHelper);
  @Mock
  private BinaryContentMapper binaryContentMapper;
  private ClothesMapper clothesMapper = new ClothesMapper(binaryContentMapper);
  private RecommendationMapper recommendationMapper = new RecommendationMapper(clothesMapper);

  @InjectMocks
  private MLModelService service; // 가상의 서비스

  private List<ClothingCondition> dummyData;

  @BeforeEach
  void setUp() {
    service = new MLModelService(clothingConditionRepository, clothingConditionRepositoryCustom, clothingConditionMapper, recommendationMapper, clothesMapper);
    List<ClothingCondition> dummys = MeaningfulDummyGenerator.generateMeaningfulDummyList();

    dummyData = dummys.stream()
        .map(clothingConditionMapper::withVector)
        .toList();
  }

  @Test
  @DisplayName("더미데이터 16개로 학습")
  void train_predicate() throws XGBoostError {
    // given
    long start = System.nanoTime();

    given(clothingConditionRepository.findAll()).willReturn(dummyData);

    service.trainModel();
    float prediction1 = service.predictSingle(dummyData.getFirst());
    float prediction2 = service.predictSingle(dummyData.getLast());

    System.out.println("추천 의상 샘플에 대한 추천 확률: " + prediction1);
    System.out.println("비추천 의상 샘플에 대한 추천 확률: " + prediction2);

    long end = System.nanoTime();
    System.out.println("실행 시간(ns): " + (end - start) / 1000000000.0 + "초");

  }

}
