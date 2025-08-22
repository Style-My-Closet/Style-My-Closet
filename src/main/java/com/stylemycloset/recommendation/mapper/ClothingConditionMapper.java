package com.stylemycloset.recommendation.mapper;


import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.ClothingCondition.ClothingConditionBuilder;
import com.stylemycloset.recommendation.util.ClothingConditionBuilderHelper;
import com.stylemycloset.recommendation.util.ConditionVectorizer;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.weather.entity.Weather;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothingConditionMapper {

    private final ConditionVectorizer conditionVectorizer;
    private final ClothingConditionBuilderHelper clothingConditionBuilderHelper;

    public ClothingCondition from3Entity(List<ClothingAttributeValue> clothingAttributes, Weather weather, User user, Boolean label) {
        ClothingCondition.ClothingConditionBuilder builder =  ClothingCondition.builder()
            .temperature(weather.getTemperature().getCurrent())
            .humidity(weather.getHumidity().getCurrent())
            .windSpeed(weather.getWindSpeed().getCurrent())
            .skyStatus(weather.getSkyStatus())
            .weatherType(weather.getAlertType())
            .gender(user.getGender())
            .temperatureSensitivity(user.getTemperatureSensitivity())
            .label(label);

        ClothingCondition.ClothingConditionBuilder builder2 =
            clothingConditionBuilderHelper.addClothingAttributes(builder,clothingAttributes);

        ClothingCondition feature = builder2.build();

        float[] embedding = conditionVectorizer.toConditionVector(feature);

        feature = builder.embedding(embedding).build();
        return  feature;


    }

    public ClothingCondition withVector(ClothingCondition cc) {
        float[] embedding = conditionVectorizer.toConditionVector(cc);
        ClothingConditionBuilder builder = ClothingCondition.builder()
            .temperature(cc.getTemperature())
            .humidity(cc.getHumidity())
            .windSpeed(cc.getWindSpeed())
            .weatherType(cc.getWeatherType())
            .gender(cc.getGender())
            .temperatureSensitivity(cc.getTemperatureSensitivity())
            .pantsLength(cc.getPantsLength())
            .sleeveLength(cc.getSleeveLength())
            .skyStatus(cc.getSkyStatus())
            .label(cc.getLabel());
        return builder.embedding(embedding).build();
    }
}
