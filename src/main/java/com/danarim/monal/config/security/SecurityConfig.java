package com.danarim.monal.config.security;

import com.danarim.monal.config.security.auth.CustomAuthenticationProvider;
import com.danarim.monal.config.security.auth.CustomUserDetailsService;
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

import static com.danarim.monal.config.WebConfig.BACKEND_PREFIX;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    //    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final CustomUserDetailsService userDetailsService;
//    private final SecurityProperties securityProperties;

    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider,
//                          AuthenticationFailureHandler authenticationFailureHandler,
                          CustomUserDetailsService userDetailsService
//                          SecurityProperties securityProperties
    ) {
        this.customAuthenticationProvider = customAuthenticationProvider;
//        this.authenticationFailureHandler = authenticationFailureHandler;
        this.userDetailsService = userDetailsService;
//        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { //skipcq: JAVA-W1042
//        if (securityProperties.getRememberMeKey() == null) {
//            throw new IllegalStateException("Remember-me key is not set");
//        }

        http
                .csrf().disable()
                .authorizeRequests(authz -> authz
                        .mvcMatchers(
                                BACKEND_PREFIX + "/registration"
                        ).permitAll()
                        .mvcMatchers(BACKEND_PREFIX + "/**").authenticated()
                        .mvcMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(formLoginConfigurer -> formLoginConfigurer
                                .loginProcessingUrl(BACKEND_PREFIX + "/login")
//                        .loginPage("/login").permitAll()
                                .usernameParameter("username")
                                .passwordParameter("password")
//                        .defaultSuccessUrl("", true)
//                        .failureHandler(authenticationFailureHandler)
                )
                .logout(logoutConfigurer -> logoutConfigurer
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("JSESSIONID", "remember-me")
                );
//                .rememberMe(rememberMeConfigurer -> rememberMeConfigurer
//                        .key(securityProperties.getRememberMeKey())
//                        .rememberMeParameter("remember-me")
//                        .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(21))
//                        .userDetailsService(userDetailsService)
//                        .useSecureCookie(true)
//                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception { //skipcq: JAVA-W1042
        return http
                .getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(customAuthenticationProvider)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

}
