package com.stylemycloset.recommendation.service;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.TrainingData;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import ml.dmlc.xgboost4j.java.*;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MLModelService {

    private Booster booster;  // XGBoost 모델
    private final ClothingConditionRepository repository;

    public MLModelService(ClothingConditionRepository repository) {
        this.repository = repository;
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

    // 학습 데이터 DTO

}