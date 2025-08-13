package com.stylemycloset.recommendation.service;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.TrainingData;
import com.stylemycloset.recommendation.repository.ClothingFeatureRepository;
import com.stylemycloset.recommendation.util.ClothingVectorizer;
import com.stylemycloset.recommendation.util.WeatherVectorizer;
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

    private float[] toConditionVector(ClothingCondition cc) {
        int alertVectorSize = WeatherVectorizer.ALERT_TYPE_SIZE;
        int skyVectorSize = WeatherVectorizer.SKY_STATUS_SIZE;
        int clothVectorSize = ClothingVectorizer.VECTOR_SIZE;

        int featureLength = 3  // temperature, windSpeed, humidity
            + alertVectorSize
            + skyVectorSize
            + 2  // gender, temperatureSensitivity
            + clothVectorSize;

        float[] features = new float[featureLength];
        int idx = 0;

        features[idx++] = (float) cc.getTemperature();
        features[idx++] = (float) cc.getWindSpeed();
        features[idx++] = (float) cc.getHumidity();

        float[] alertVec = WeatherVectorizer.vectorizeAlertType(cc.getWeatherType());
        for (float v : alertVec) {
            features[idx++] = v;
        }

        float[] skyVec = WeatherVectorizer.vectorizeSkyStatus(cc.getSkyStatus());
        for (float v : skyVec) {
            features[idx++] = v;
        }

        features[idx++] = cc.getGender() == Gender.MALE ? 1f : 0f;
        features[idx++] = cc.getTemperatureSensitivity() != null ? cc.getTemperatureSensitivity() : 0f;

        float[] clothVec = ClothingVectorizer.vectorize(cc.getColor(),cc.getSleeveLength(),cc.getPantsLength());
        for (float v : clothVec) {
            features[idx++] = v;
        }

        return features;
    }

    // DB에서 학습 데이터 읽어옴
    public TrainingData loadTrainingData() {
        List<ClothingCondition> conditions = repository.findAll();
        if (conditions.isEmpty()) return null;

        int n = conditions.size();
        int conditionLength = toConditionVector(conditions.get(0)).length;

        float[][] x = new float[n][conditionLength];
        int[] y = new int[n];

        for (int i = 0; i < n; i++) {
            x[i] = toConditionVector(conditions.get(i));
            y[i] = conditions.get(i).getLabel() ? 1 : 0;
        }

        return new TrainingData(x, y);
    }

    public float predictSingle(ClothingCondition cf) throws XGBoostError {
        float[] features = toConditionVector(cf);
        DMatrix dMatrix = new DMatrix(features, 1, features.length, Float.NaN);
        float[][] predictions = booster.predict(dMatrix);
        return predictions[0][0];
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
