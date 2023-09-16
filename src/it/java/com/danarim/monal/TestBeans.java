package com.danarim.monal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

import static com.danarim.monal.config.WebConfig.SQL_INIT_SCRIPTS;

/**
 * Configuring beans for tests that are not a SpringBootTest.
 */
@TestConfiguration
class TestBeans {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // Same as in WebConfig
    @Bean
    public DataSourceInitializer dataSourceInitializer(
            @Qualifier("dataSource") DataSource dataSource
    ) {
        ResourceDatabasePopulator resourceDbPopulator = new ResourceDatabasePopulator();

        for (String script : SQL_INIT_SCRIPTS) {
            resourceDbPopulator.addScript(new ClassPathResource("/sql/" + script));
        }

        DataSourceInitializer sourceInitializer = new DataSourceInitializer();
        sourceInitializer.setDataSource(dataSource);
        sourceInitializer.setDatabasePopulator(resourceDbPopulator);
        return sourceInitializer;
    }

}
