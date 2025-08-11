package com.stylemycloset.common.config;


import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayRepairConfig implements FlywayConfigurationCustomizer {

    @Override
    public void customize(FluentConfiguration configuration) {
        Flyway flyway = new Flyway(configuration);
        flyway.repair();
    }
}
