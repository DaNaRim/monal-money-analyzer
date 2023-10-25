package com.danarim.monal.config;

import com.danarim.monal.util.CookieUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Application configuration.
 */
@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    //If you change list count of supported locales, you should also change
    // SUPPORTED_LOCALE_COUNT in ValidPasswordValidatorTest
    public static final List<Locale> SUPPORTED_LOCALES = Collections.unmodifiableList(Arrays.asList(
            DEFAULT_LOCALE,
            new Locale("uk")
    ));

    public static final String API_V1_PREFIX = "/api/v1";

    // Public because it is used in tests.
    public static final List<String> SQL_INIT_SCRIPTS = List.of(
            "data-roles.sql",
            "data-categories.sql"
    );

    private static final List<String> FRONTEND_URLS = List.of(
            "/",
            "/{x:[\\w\\-]+}",
            "/{x:^(?!api$).*$}/*/{y:[\\w\\-]+}"
    );

    private static final List<String> MESSAGES = List.of(
            "validation",
            "errors",
            "mail",
            "messages"
    );

    @PostConstruct
    public static void init() {
        // Setting Spring Boot SetTimeZone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LocaleChangeInterceptor());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html",
                                    "/favicon.ico",
                                    "/logo192.png",
                                    "/logo512.png",
                                    "/manifest.json",
                                    "/robots.txt")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String url : FRONTEND_URLS) {
            registry.addViewController(url).setViewName("forward:/index.html");
        }
    }

    @Override
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean validatorBean = new LocalValidatorFactoryBean();
        validatorBean.setValidationMessageSource(messageSource());
        return validatorBean;
    }

    /**
     * Bean for locale resolver.
     *
     * @return LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(DEFAULT_LOCALE);
        localeResolver.setCookieName(CookieUtil.COOKIE_LOCALE_KEY);
        localeResolver.setCookieHttpOnly(false); //for frontend access
        localeResolver.setCookieSecure(true);
        return localeResolver;
    }

    /**
     * Bean for message source.
     *
     * @return MessageSource
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();

        String[] baseNames =
                MESSAGES.stream().map(name -> "classpath:/i18n/" + name).toArray(String[]::new);

        messageSource.setBasenames(baseNames);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Bean for data source. It is used for database initialization.
     *
     * @param dataSource DataSource
     *
     * @return DataSourceInitializer
     */
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

    /**
     * Bean for email template resolver. It has the highest priority.
     *
     * @return ClassLoaderTemplateResolver for email templates
     */
    @Bean
    public ClassLoaderTemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("mail/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setOrder(0);
        templateResolver.setCheckExistence(true);

        return templateResolver;
    }

}
