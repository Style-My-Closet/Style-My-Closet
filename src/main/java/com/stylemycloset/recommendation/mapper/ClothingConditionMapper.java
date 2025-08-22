package com.stylemycloset.recommendation.mapper;

import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.repository.ClothRepository;
import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import com.stylemycloset.recommendation.dto.RecommendationDto;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.ClothingCondition.ClothingConditionBuilder;
import com.stylemycloset.recommendation.util.ClothingConditionBuilderHelper;
import com.stylemycloset.recommendation.util.ConditionVectorizer;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;

@Mapper
@RequiredArgsConstructor
public class ClothingConditionMapper {

    private final ConditionVectorizer conditionVectorizer;

    public ClothingCondition from3Entity(Cloth cloth, Weather weather, User user) {
        ClothingCondition.ClothingConditionBuilder builder =  ClothingCondition.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .windSpeed(weather.getWindSpeed().getCurrent())
            .weatherType(weather.getAlertType())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(false);

        ClothingCondition.ClothingConditionBuilder builder2 =
            ClothingConditionBuilderHelper.addClothingAttributes(builder,cloth.getAttributeValues());

        ClothingCondition feature = builder2.build();

        float[] embedding = conditionVectorizer.toConditionVector(feature);

        feature = builder.embedding(embedding).build();
        return  feature;


    }


}
