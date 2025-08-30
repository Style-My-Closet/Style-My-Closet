package com.stylemycloset.common.util;

import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectableRepository;
import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.entity.Color;
import com.stylemycloset.recommendation.entity.Length;
import com.stylemycloset.recommendation.entity.Material;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.recommendation.util.MeaningfulDummyGenerator;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClothingConditionRepository clothingConditionRepository;
    private final ClothingConditionMapper clothingConditionMapper;
    private final ClothesAttributeDefinitionRepository definitionRepository;
    private final ClothesAttributeDefinitionSelectableRepository valueRepository;

    @Override
    public void run(String... args) {
        if (clothingConditionRepository.count() == 0) {
            List<ClothingCondition> dummyData;
            List<ClothingCondition> dummys = MeaningfulDummyGenerator.generateMeaningfulDummyList();

            dummyData = dummys.stream()
                .map(clothingConditionMapper::withVector)
                .toList();

            clothingConditionRepository.saveAll(dummyData);

        }
        // enum 기반 속성 초기화
        initializeAttribute("color", Color.values());
        initializeAttribute("length", Length.values());
        initializeAttribute("material", Material.values());
    }

    private <E extends Enum<E>> void initializeAttribute(String attributeName, E[] values) {
        // definition 조회 또는 생성
        ClothesAttributeDefinition definition = definitionRepository.findByName(attributeName)
            .orElseGet(() -> {
                ClothesAttributeDefinition def = new ClothesAttributeDefinition(attributeName,
                    new ArrayList<String>());
                return definitionRepository.save(def);
            });

        // 선택값 초기화
        for (E val : values) {
            String valueStr = val.name();
            boolean exists = valueRepository.existsByDefinitionAndValue(definition, valueStr);
            if (!exists) {
                ClothesAttributeSelectableValue newValue = new ClothesAttributeSelectableValue(definition, valueStr);
                valueRepository.save(newValue);
            }
        }
    }
}