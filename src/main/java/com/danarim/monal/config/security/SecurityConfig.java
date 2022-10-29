package com.danarim.monal.config.security;

import com.danarim.monal.config.security.filters.CustomAuthenticationFilter;
import com.danarim.monal.config.security.filters.CustomAuthorizationFilter;
import com.danarim.monal.config.security.filters.JwtRefreshFilter;
import com.danarim.monal.failHandler.CustomAuthFailureHandler;
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

import static com.danarim.monal.config.WebConfig.BACKEND_PREFIX;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final ApplicationContext context;

    public SecurityConfig(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { //skipcq: JAVA-W1042
        http
                .authorizeRequests(authz -> authz
                        .mvcMatchers(
                                BACKEND_PREFIX + "/login",
                                BACKEND_PREFIX + "/jwtTokenRefresh",
                                BACKEND_PREFIX + "/registration"
                        ).permitAll()
                        .mvcMatchers(BACKEND_PREFIX + "/registration").authenticated()
                        .mvcMatchers(BACKEND_PREFIX + "/**").authenticated()
                        .mvcMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf().disable() //TODO enable csrf
                .sessionManagement(sessionConfigurer -> sessionConfigurer
                        .sessionCreationPolicy(STATELESS)
                )
                .addFilter(customAuthenticationFilter(http))
                .addFilterBefore(
                        context.getBean(CustomAuthorizationFilter.class), UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(context.getBean(JwtRefreshFilter.class), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter(HttpSecurity http) throws Exception { //skipcq: JAVA-W1042
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(
                authenticationManager(http),
                context.getBean(CustomAuthFailureHandler.class),
                context.getBean(JwtUtil.class)
        );
        filter.setFilterProcessesUrl(BACKEND_PREFIX + "/login");
        filter.setAuthenticationFailureHandler(context.getBean(AuthenticationFailureHandler.class));
        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception { //skipcq: JAVA-W1042
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

}
