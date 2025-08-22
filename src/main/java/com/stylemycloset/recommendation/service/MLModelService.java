package com.stylemycloset.recommendation.service;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.TrainingData;
import com.stylemycloset.recommendation.mapper.ClothesMapper;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.mapper.RecommendationMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import lombok.RequiredArgsConstructor;
import ml.dmlc.xgboost4j.java.*;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MLModelService {


    private Booster booster;  // XGBoost 모델
    private final ClothingConditionRepository repository;
    private final ClothingConditionMapper clothingConditionMapper;

    public RecommendationDto prediction(List<Cloth> clothes, Weather weather, User user)
        throws XGBoostError {
        RecommendationDto current = RecommendationMapper.parseToRecommendationDto(clothes,weather,user);
        RecommendationDto result = null;
        trainModel();
        for(Cloth c : clothes) {
            double p =  predictSingle(clothingConditionMapper.from3Entity(c.getAttributeValues(),weather,user,false));
            if(!(p>70)){
                current.clothes().remove(ClothesMapper.toClothesDto(c));
                result = current;
                recordFeedback(weather,user,c.getAttributeValues(),false);
            }else {
              recordFeedback(weather,user,c.getAttributeValues(),true);
            }
        }
        return result;
    }
    // 단일 예측
    public float predictSingle(float[] embedding) throws XGBoostError {
        DMatrix dMatrix = new DMatrix(embedding, 1, embedding.length, Float.NaN);
        float[][] predictions = booster.predict(dMatrix);
        return predictions[0][0];
    }

    // ClothingCondition 기반 예측
    public float predictSingle(ClothingCondition cf) throws XGBoostError {
        if (cf.getEmbedding() == null) {
            throw new IllegalArgumentException("ClothingCondition embedding is null");
        }
        return predictSingle(cf.getEmbedding());
    }

    // 모델 학습
    public void trainModel() throws XGBoostError {
        TrainingData data = loadTrainingData();
        if (data == null) return;

        int nSamples = data.features.length;
        int nFeatures = data.features[0].length;

        // 2차원 float 배열 → 1차원 배열 변환
        float[] features1D = new float[nSamples * nFeatures];
        for (int i = 0; i < nSamples; i++) {
            System.arraycopy(data.features[i], 0, features1D, i * nFeatures, nFeatures);
        }

        // int[] 라벨 → float[] 라벨
        float[] labelsFloat = new float[data.labels.length];
        for (int i = 0; i < data.labels.length; i++) {
            labelsFloat[i] = data.labels[i];
        }

        // DMatrix 생성
        DMatrix trainMat = new DMatrix(features1D, nSamples, nFeatures, Float.NaN);
        trainMat.setLabel(labelsFloat);

        Map<String, Object> params = new HashMap<>();
        params.put("eta", 0.1);
        params.put("max_depth", 6);
        params.put("objective", "binary:logistic");
        params.put("eval_metric", "logloss");

        HashMap<String, DMatrix> watches = new HashMap<>();
        watches.put("train", trainMat);

        booster = XGBoost.train(trainMat, params, 10, watches, null, null);
    }
    // DB에서 학습 데이터 읽어오기 (embedding 컬럼 활용)
    private TrainingData loadTrainingData() {
        List<ClothingCondition> conditions = repository.findAll();
        if (conditions.isEmpty()) return null;

        int nSamples = conditions.size();
        int nFeatures = conditions.get(0).getEmbedding().length;

        float[][] x = new float[nSamples][nFeatures];
        int[] y = new int[nSamples];

        for (int i = 0; i < nSamples; i++) {
            x[i] = conditions.get(i).getEmbedding(); // embedding 바로 사용
            y[i] = conditions.get(i).getLabel() ? 1 : 0;
        }

        return new TrainingData(x, y);
    }

    // 사용자 피드백 데이터 저장
    private void recordFeedback(Weather weather, User user, List<ClothingAttributeValue> values, Boolean label) {

        ClothingCondition feature = clothingConditionMapper.from3Entity(values, weather, user, label);

        repository.save(feature);
    }
}