package com.stylemycloset.recommendation.service;

import com.stylemycloset.recommendation.entity.ClothingFeature;
import com.stylemycloset.recommendation.entity.TrainingData;
import com.stylemycloset.recommendation.repository.ClothingFeatureRepository;
import com.stylemycloset.user.entity.Gender;
import java.util.*;
import lombok.RequiredArgsConstructor;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MLModelService {
    private final ClothingFeatureRepository repository;
    private Booster booster;  // XGBoost 모델 (예시)

    // DB에서 학습 데이터 읽어옴
    public TrainingData loadTrainingData() {
        List<ClothingFeature> features = repository.findAll();

        int n = features.size();
        int featureLength = 5;  // temperature, windSpeed, humidity, gender, temperatureSensitivity
        float[][] x = new float[n][featureLength];
        int[] y = new int[n];

        for (int i = 0; i < n; i++) {
            ClothingFeature f = features.get(i);
            x[i][0] = (float) f.getTemperature();
            x[i][1] = (float) f.getWindSpeed();
            x[i][2] = (float) f.getHumidity();
            x[i][3] = f.getGender() == Gender.MALE ? 1f : 0f;  // 예시로 이진 인코딩
            x[i][4] = f.getTemperatureSensitivity() != null ? f.getTemperatureSensitivity() : 0f;

            y[i] = f.getLabel() ? 1 : 0;
        }

        return new TrainingData(x, y);
    }

    public void trainModel() throws XGBoostError {
        TrainingData data = loadTrainingData();

        // 2차원 float 배열 → 1차원 float 배열 변환
        float[][] features2D = data.features;
        int nSamples = features2D.length;
        int nFeatures = features2D[0].length;

        float[] features1D = new float[nSamples * nFeatures];
        for (int i = 0; i < nSamples; i++) {
            for (int j = 0; j < nFeatures; j++) {
                features1D[i * nFeatures + j] = features2D[i][j];
            }
        }

        // int[] 라벨 → float[] 라벨 변환
        int[] labelsInt = data.labels;
        float[] labelsFloat = new float[labelsInt.length];
        for (int i = 0; i < labelsInt.length; i++) {
            labelsFloat[i] = labelsInt[i];
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

}
