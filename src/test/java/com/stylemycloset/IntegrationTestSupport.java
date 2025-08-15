package com.stylemycloset;

import com.stylemycloset.testconfig.TestStorageConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestStorageConfig.class)
public abstract class IntegrationTestSupport extends TestContainerSupport {

}
