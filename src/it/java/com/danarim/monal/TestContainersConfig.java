package com.danarim.monal;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Used to set up test containers.
 */
@TestConfiguration
public class TestContainersConfig {

    @Container
    public static final GenericContainer postgreSQLContainer
            = new PostgreSQLContainer("postgres:15.3")
            .withDatabaseName("db")
            .withUsername("test")
            .withPassword("test");

    static {
        postgreSQLContainer.start();
    }

}
