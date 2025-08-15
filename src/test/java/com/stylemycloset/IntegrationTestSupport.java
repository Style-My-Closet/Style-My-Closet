package com.stylemycloset;

import com.stylemycloset.testconfig.TestImageStorageConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestImageStorageConfig.class)
public abstract class IntegrationTestSupport extends TestContainerSupport {

}
