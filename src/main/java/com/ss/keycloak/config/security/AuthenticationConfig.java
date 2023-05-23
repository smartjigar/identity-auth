package com.ss.keycloak.config.security;

import com.ss.keycloak.utils.constant.AppConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@EnableWebSecurity
@Configuration
@AllArgsConstructor
@EnableMethodSecurity()
public class AuthenticationConfig {

    @Autowired
    private MyRequestMatcher myRequestMatcher;

    private final SecurityConfigProperties configProperties;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;


    @Bean
    @ConditionalOnProperty(value = "app.security.enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher(AppConstants.APP_CONTEXT_PATH, HttpMethod.OPTIONS.name())).permitAll()
                //.requestMatchers(new AntPathRequestMatcher("/")).permitAll() // forwards to swagger
                .requestMatchers(new AntPathRequestMatcher("/docs/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/ui/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/health/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher(AppConstants.APP_CONTEXT_PATH + "/monitoring/**")).permitAll()
                //.requestMatchers(new AntPathRequestMatcher(AppConstants.APP_CONTEXT_PATH + "/asset/**")).hasRole("ASSET")
                .requestMatchers(this.myRequestMatcher).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                .and().oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(new CustomAuthenticationConverter(this.configProperties.clientId()));
        return http.build();
    }


    @Bean(value = "apiLists")
    public List<String> apiLists() {
        return this.requestMappingHandlerMapping
                .getHandlerMethods()
                .keySet()
                .stream()
                .filter(p -> p.getActivePatternsCondition().toString().contains(AppConstants.APP_CONTEXT_PATH))
                .map(p -> p.getMethodsCondition().getMethods().iterator().next().toString().toLowerCase()
                        + p.getActivePatternsCondition().toString().replace("/", "_").replaceAll("\\[|\\]", ""))
                .collect(Collectors.toList());
    }
    
    @Bean
    protected Keycloak keycloakClient() {
        return KeycloakBuilder.builder()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .realm(this.configProperties.realm())
                .clientId(this.configProperties.clientId())
                .clientSecret(this.configProperties.secret())
                .serverUrl(this.configProperties.authServerUrl())
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "app.security.enabled", havingValue = "false")
    public WebSecurityCustomizer securityCustomizer() {
        log.warn("Disable security : This is not recommended to use in production environments.");
        return web -> web.ignoring().requestMatchers(new AntPathRequestMatcher("**"));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(this.configProperties.corsOrigins());
        configuration.setAllowedMethods(List.of("HEAD", "OPTIONS", "GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

