package com.stylemycloset.recommendation.repository;

import com.stylemycloset.recommendation.dto.ConditionWithDistance;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.PantsLength;
import com.stylemycloset.recommendation.entity.SleeveLength;
import com.stylemycloset.recommendation.util.VectorHelper;
import com.stylemycloset.user.entity.Gender;
import com.stylemycloset.weather.entity.Weather.AlertType;
import com.stylemycloset.weather.entity.Weather.SkyStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

@Repository
public class ClothingConditionRepositoryImpl implements ClothingConditionRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager em;

    @Autowired
    public ClothingConditionRepositoryImpl(JdbcTemplate jdbcTemplate, EntityManager em) {
        this.jdbcTemplate = jdbcTemplate;
        this.em = em;
    }

    @Override
    public ClothingCondition findMostSimilarByVector(float[] inputVector) {
        String sql = "SELECT * FROM clothing_conditions ORDER BY ABS(embedding <#> ?) LIMIT 1";

        PGobject pg = VectorHelper.toPGVector(inputVector);

        return jdbcTemplate.queryForObject(sql,
            // vector 안전 바인딩
            (rs, rowNum) -> {
                return ClothingCondition.builder()
                    .id(rs.getLong("id"))
                    .temperature(rs.getDouble("temperature"))
                    .humidity(rs.getDouble("humidity"))
                    .windSpeed(rs.getDouble("wind_speed"))
                    .gender(Gender.values()[rs.getInt("gender")])
                    .temperatureSensitivity(rs.getInt("temperature_sensitivity"))
                    .skyStatus(SkyStatus.values()[rs.getInt("sky_status")])
                    .weatherType(AlertType.values()[rs.getInt("weather_type")])
                    .color(Color.values()[rs.getInt("color")])
                    .sleeveLength(SleeveLength.values()[rs.getInt("sleeve_length")])
                    .pantsLength(PantsLength.values()[rs.getInt("pants_length")])
                    .label(rs.getBoolean("label"))
                    .embedding(VectorHelper.fromPGVector(rs.getString("embedding")))
                    .build();
            }, pg
        );
    }

    @Override
    public boolean saveIfNotDuplicate(ClothingCondition newCondition) {
        String sql = """
            SELECT id, embedding, ABS(embedding <#> ?) AS cosine_distance
            FROM clothing_conditions
            ORDER BY cosine_distance
            LIMIT 3
            """;

        List<ConditionWithDistance> result = jdbcTemplate.query(
            sql,
            ps -> VectorHelper.setVector(ps, 1, newCondition.getEmbedding()),
            (rs, rowNum) -> new ConditionWithDistance(
                rs.getLong("id"),
                VectorHelper.fromPGVector(rs.getString("embedding")),
                rs.getDouble("cosine_distance")
            )
        );

        if (!result.isEmpty() && result.get(0).cosineDistance() < 0.001) {
            System.out.println("유사 벡터 발견, 거리: " + result.get(0).cosineDistance());
            return false; // 거의 동일한 벡터 존재 → 저장하지 않음
        }

        em.persist(newCondition);
        return true;
    }
}
