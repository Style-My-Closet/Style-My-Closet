package com.stylemycloset.recommendation.entity;

public class TrainingData {
    public float[][] features;
    public int[] labels;

    public TrainingData(float[][] features, int[] labels) {
        this.features = features;
        this.labels = labels;
    }
}

