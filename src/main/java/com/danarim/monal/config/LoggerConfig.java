package com.danarim.monal.config;

import com.danarim.monal.exceptions.ServerStartupException;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;

import static com.danarim.monal.config.LoggerConfig.Log4j2ConfigurationPropertyNames.*;
import static org.apache.logging.log4j.core.appender.ConsoleAppender.Target.SYSTEM_OUT;

@Plugin(name = "CustomConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public final class LoggerConfig extends ConfigurationFactory {

    private static final String LOGS_FOLDER = "./logs/";

    private static final String CONSOLE_APPENDER = "CONSOLE";
    private static final String CONSOLE_LOG_PATTERN
            = "%d %highlight{%-5p} %pid [%15.15thread] %style{%40.40logger{1}}{blue} : %m %throwable{short}%n";
    private static final Level CONSOLE_LEVEL = Level.INFO;

    private static final String ROLLING_FILE_APPENDER = "ROLLING_FILE";
    private static final String ROLLING_FILE_NAME = "rolling.log";
    private static final String ROLLING_FILE_FOLDER = LOGS_FOLDER + "rolling/";
    private static final String ROLLING_FILE_FOLDER_PATTERN = ROLLING_FILE_FOLDER + "%d{yyyy-MM-dd}/";
    private static final String ROLLING_FILE_NAME_PATTERN = "rolling-%d{yyyy-MM-dd}-%i.log.gz";
    private static final String ROLLING_FILE_INTERVAL = "1";
    private static final String ROLLING_FILE_LOG_PATTERN
            = "%d %highlight{%-5p} %pid [%20.20thread] %style{%40.40logger{3.}}{blue} : %m %n%throwable";
    private static final String ROLLING_FILE_MAX_SIZE = "100MB";
    private static final String ROLLING_FILE_MAX_COUNT = "10";
    private static final Level ROLLING_FILE_LEVEL = Level.DEBUG;

    private static final String FILE_APPENDER = "FILE";
    private static final String FILE_NAME = "logs.log";
    private static final String FILE_LOG_PATTERN
            = "%d %highlight{%-5p} %pid [%15.15thread] %style{%40.40logger{1}}{blue} : %m %n%throwable{5}";
    private static final Level FILE_LEVEL = Level.INFO;

    private static final boolean IS_SMTP_ENABLED = false;
    private static final String SMTP_APPENDER = "SMTP";
    private static final String SMTP_SUBJECT = "Smart Parking Error";
    private static final String SMTP_TO = "rangar.danarim@gmail.com";
    private static final String SMTP_LOG_PATTERN = "%d%n %m%n %throwable";
    private static final Level SMTP_LEVEL = Level.ERROR;

    private static final String SMTP_LOGGER_NAME = "com";

    @Override
    protected String[] getSupportedTypes() {
        return new String[]{"*"};
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext, final ConfigurationSource source) {
        return getConfiguration(loggerContext, source.toString(), null);
    }

    @Override
    public Configuration getConfiguration(final LoggerContext loggerContext,
                                          final String name,
                                          final URI configLocation
    ) {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
        return createConfiguration(name, builder);
    }

    private static Configuration createConfiguration(final String name,
                                                     final ConfigurationBuilder<BuiltConfiguration> builder
    ) {

        builder.setConfigurationName(name);
//        builder.setMonitorInterval("5");

        builder.add(getConsoleAppender(builder));
        builder.add(getRollingFileAppender(builder));
        builder.add(getFileAppender(builder));


        System.setProperty("mail.smtp.starttls.enable", "true");

        RootLoggerComponentBuilder rootLogger = builder.newAsyncRootLogger(Level.ALL)
                .add(builder.newFilter(ATTRIBUTE_THRESHOLD_FILTER, Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                        .addAttribute(ATTRIBUTE_LEVEL, Level.ALL)
                )
                .add(builder.newAppenderRef(CONSOLE_APPENDER).addAttribute(ATTRIBUTE_LEVEL, CONSOLE_LEVEL))
                .add(builder.newAppenderRef(FILE_APPENDER).addAttribute(ATTRIBUTE_LEVEL, FILE_LEVEL))
                .add(builder.newAppenderRef(ROLLING_FILE_APPENDER).addAttribute(ATTRIBUTE_LEVEL, ROLLING_FILE_LEVEL));

        builder.add(rootLogger);

        if (IS_SMTP_ENABLED) {
            builder.add(getSmtpAppender(builder));
            LoggerComponentBuilder asyncLogger = builder.newAsyncLogger(SMTP_LOGGER_NAME, Level.ALL)
                    .add(builder.newFilter(ATTRIBUTE_THRESHOLD_FILTER, Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                            .addAttribute(ATTRIBUTE_LEVEL, Level.ALL)
                    )
                    .add(builder.newAppenderRef(SMTP_APPENDER).addAttribute(ATTRIBUTE_LEVEL, SMTP_LEVEL));

            builder.add(asyncLogger);
        }
        return builder.build();
    }

    private static AppenderComponentBuilder getConsoleAppender(ConfigurationBuilder<BuiltConfiguration> builder) {
        return builder.newAppender(CONSOLE_APPENDER, ATTRIBUTE_CONSOLE_PLUGIN)
                .addAttribute(ATTRIBUTE_TARGET, SYSTEM_OUT)
                .add(builder.newLayout(ATTRIBUTE_PATTERN_LAYOUT)
                        .addAttribute(ATTRIBUTE_PATTERN, CONSOLE_LOG_PATTERN)
                        .addAttribute(ATTRIBUTE_DISABLE_ANSI, false)
                );
    }

    private static AppenderComponentBuilder getRollingFileAppender(ConfigurationBuilder<BuiltConfiguration> builder) {
        return builder.newAppender(ROLLING_FILE_APPENDER, ATTRIBUTE_ROLLING_FILE_PLUGIN)
                .addAttribute(ATTRIBUTE_FILE_NAME, ROLLING_FILE_FOLDER + ROLLING_FILE_NAME)
                .addAttribute(ATTRIBUTE_FILE_PATTERN, ROLLING_FILE_FOLDER_PATTERN + ROLLING_FILE_NAME_PATTERN)
                .add(builder.newLayout(ATTRIBUTE_PATTERN_LAYOUT)
                        .addAttribute(ATTRIBUTE_PATTERN, ROLLING_FILE_LOG_PATTERN)
                        .addAttribute(ATTRIBUTE_DISABLE_ANSI, false)
                )
                .addComponent(builder.newComponent(ATTRIBUTE_POLICIES)
                        .addComponent(builder.newComponent(ATTRIBUTE_SIZE_BASED_TRIGGERING_POLICY)
                                .addAttribute(ATTRIBUTE_SIZE, ROLLING_FILE_MAX_SIZE)
                        )
                        .addComponent(builder.newComponent(ATTRIBUTE_TIME_BASED_TRIGGERING_POLICY)
                                .addAttribute(ATTRIBUTE_INTERVAL, ROLLING_FILE_INTERVAL)
                        )
                )
                .addComponent(builder.newComponent(ATTRIBUTE_DEFAULT_ROLLOVER_STRATEGY)
                        .addAttribute(ATTRIBUTE_MAX, ROLLING_FILE_MAX_COUNT)
                );
    }

    private static AppenderComponentBuilder getFileAppender(ConfigurationBuilder<BuiltConfiguration> builder) {
        return builder.newAppender(FILE_APPENDER, ATTRIBUTE_FILE_PLUGIN)
                .addAttribute(ATTRIBUTE_FILE_NAME, LOGS_FOLDER + FILE_NAME)
                .add(builder.newLayout(ATTRIBUTE_PATTERN_LAYOUT)
                        .addAttribute(ATTRIBUTE_PATTERN, FILE_LOG_PATTERN)
                        .addAttribute(ATTRIBUTE_DISABLE_ANSI, false)
                );
    }

    private static AppenderComponentBuilder getSmtpAppender(ConfigurationBuilder<BuiltConfiguration> builder) {
        HashMap<String, String> properties = getSecurityProperties();
        String username = properties.get("secrets.mail-username"); //TODO refactor
        String password = properties.get("secrets.mail-password");

        return builder.newAppender(SMTP_APPENDER, ATTRIBUTE_SMTP_PLUGIN)
                .addAttribute(ATTRIBUTE_SUBJECT, SMTP_SUBJECT)
                .addAttribute(ATTRIBUTE_TO, SMTP_TO)
                .addAttribute(ATTRIBUTE_FROM, username)
                .addAttribute(ATTRIBUTE_SMTP_HOST, "smtp.gmail.com")
                .addAttribute(ATTRIBUTE_SMTP_PORT, "587")
                .addAttribute(ATTRIBUTE_SMTP_USERNAME, username)
                .addAttribute(ATTRIBUTE_SMTP_PASSWORD, password)
                .addAttribute(ATTRIBUTE_SMTP_PROTOCOL, "smtp")
                .add(builder.newLayout(ATTRIBUTE_PATTERN_LAYOUT)
                        .addAttribute(ATTRIBUTE_PATTERN, SMTP_LOG_PATTERN)
                );
    }

    private static HashMap<String, String> getSecurityProperties() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/secrets.properties")) {
            Properties properties = new Properties();

            properties.load(fis);

            return Maps.newHashMap(Maps.fromProperties(properties));
        } catch (IOException e) {
            throw new ServerStartupException("Error while reading security.properties", e);
        }
    }

    protected static final class Log4j2ConfigurationPropertyNames {

        static final String ATTRIBUTE_THRESHOLD_FILTER = "ThresholdFilter";
        static final String ATTRIBUTE_PATTERN_LAYOUT = "PatternLayout";
        static final String ATTRIBUTE_LEVEL = "level";
        static final String ATTRIBUTE_PATTERN = "pattern";
        static final String ATTRIBUTE_DISABLE_ANSI = "disableAnsi";

        static final String ATTRIBUTE_POLICIES = "Policies";
        static final String ATTRIBUTE_SIZE_BASED_TRIGGERING_POLICY = "SizeBasedTriggeringPolicy";
        static final String ATTRIBUTE_TIME_BASED_TRIGGERING_POLICY = "TimeBasedTriggeringPolicy";

        static final String ATTRIBUTE_CONSOLE_PLUGIN = "Console";
        static final String ATTRIBUTE_TARGET = "target";

        static final String ATTRIBUTE_FILE_PLUGIN = "File";
        static final String ATTRIBUTE_FILE_NAME = "fileName";
        static final String ATTRIBUTE_FILE_PATTERN = "filePattern";

        static final String ATTRIBUTE_ROLLING_FILE_PLUGIN = "RollingFile";
        static final String ATTRIBUTE_SIZE = "size";
        static final String ATTRIBUTE_INTERVAL = "interval";
        static final String ATTRIBUTE_DEFAULT_ROLLOVER_STRATEGY = "DefaultRolloverStrategy";
        static final String ATTRIBUTE_MAX = "max";

        static final String ATTRIBUTE_SMTP_PLUGIN = "SMTP";
        static final String ATTRIBUTE_SUBJECT = "subject";
        static final String ATTRIBUTE_TO = "to";
        static final String ATTRIBUTE_FROM = "from";
        static final String ATTRIBUTE_SMTP_HOST = "smtpHost";
        static final String ATTRIBUTE_SMTP_PORT = "smtpPort";
        static final String ATTRIBUTE_SMTP_USERNAME = "smtpUsername";
        static final String ATTRIBUTE_SMTP_PASSWORD = "smtpPassword";
        static final String ATTRIBUTE_SMTP_PROTOCOL = "smtpProtocol";

        private Log4j2ConfigurationPropertyNames() {
        }
    }
}
