package com.stylemycloset.recommendation.util;

import org.postgresql.util.PGobject;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VectorHelper {

    // float[] → PGobject
    public static PGobject toPGVector(float[] vector) {
        if (vector == null) return null;

        PGobject pg = new PGobject();
        pg.setType("vector");

        String vectorString = IntStream.range(0, vector.length)
            .mapToObj(i -> Float.toString(vector[i]))
            .collect(Collectors.joining(",", "[", "]"));
        try {
            pg.setValue(vectorString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pg;
    }

    // "[0.12,0.34,...]" → float[]
    public static float[] fromPGVector(String vectorString) {
        if (vectorString == null) return null;

        vectorString = vectorString.replaceAll("[\\[\\]]", "");
        String[] parts = vectorString.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i]);
        }
        return result;
    }

    // PreparedStatement에 vector 안전하게 바인딩
    public static void setVector(java.sql.PreparedStatement ps, int index, float[] vector) throws java.sql.SQLException {
        PGobject pg = toPGVector(vector);
        if (pg == null) {
            ps.setNull(index, java.sql.Types.OTHER);
        } else {
            ps.setObject(index, pg);
        }
    }

    public static float[] normalize(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Vector is null or empty");
        }

        float norm = 0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);

        if (norm == 0f) {
            throw new IllegalArgumentException("Zero vector cannot be normalized");
        }

        float[] normalized = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            normalized[i] = vector[i] / norm;
        }

        return normalized;
    }
}
