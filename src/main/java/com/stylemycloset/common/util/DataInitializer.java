package com.stylemycloset.common.util;

import com.stylemycloset.recommendation.entity.ClothingCondition;
import com.stylemycloset.recommendation.mapper.ClothingConditionMapper;
import com.stylemycloset.recommendation.repository.ClothingConditionRepository;
import com.stylemycloset.recommendation.util.MeaningfulDummyGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClothingConditionRepository clothingConditionRepository;
    private final ClothingConditionMapper clothingConditionMapper;

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
    }
}