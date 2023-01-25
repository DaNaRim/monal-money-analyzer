package com.danarim.monal.config.security;

import com.danarim.monal.config.filters.CustomAuthenticationFilter;
import com.danarim.monal.config.filters.CustomAuthorizationFilter;
import com.danarim.monal.config.security.jwt.JwtUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.stream.Stream;

import static com.danarim.monal.config.WebConfig.API_V1_PREFIX;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Security configuration.
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    public static final List<String> PERMIT_ALL_API_ENDPOINTS = Stream.of(
            "/login",
            "/logout",
            "/auth/getState",
            "/auth/refresh",

            "/registration",
            "/registrationConfirm",
            "/resendVerificationToken",

            "/resetPassword",
            "/resetPasswordConfirm",
            "/resetPasswordSet"
    ).map(endpoint -> API_V1_PREFIX + endpoint).toList();

    private static final int BCRYPT_STRENGTH = 11;

    private final ApplicationContext context;

    public SecurityConfig(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Configure security filter chain.
     *
     * @param http http security
     *
     * @return security filter chain
     *
     * @throws Exception if something goes wrong
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authz -> authz
                        .mvcMatchers(PERMIT_ALL_API_ENDPOINTS.toArray(String[]::new)).permitAll()
                        .mvcMatchers(API_V1_PREFIX + "/**").authenticated()
                        .mvcMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .requiresChannel(channel -> channel
                        .anyRequest().requiresSecure()
                )
                .csrf().disable() //csrf handles by CustomAuthorizationFilter
                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(STATELESS)
                )
                .addFilter(customAuthenticationFilter(http))
                .addFilterBefore(
                        context.getBean(CustomAuthorizationFilter.class),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    /**
     * Configure custom authentication filter.
     *
     * @param http http security
     *
     * @return custom authentication filter
     *
     * @throws Exception if something goes wrong
     */
    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(HttpSecurity http)
            throws Exception {
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(
                authenticationManager(http),
                context.getBean(JwtUtil.class)
        );
        filter.setFilterProcessesUrl(API_V1_PREFIX + "/login");
        filter.setAuthenticationFailureHandler(context.getBean(AuthenticationFailureHandler.class));
        return filter;
    }

    /**
     * Bean for authentication manager.
     *
     * @param http http security
     *
     * @return authentication manager
     *
     * @throws Exception if something goes wrong
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

}
