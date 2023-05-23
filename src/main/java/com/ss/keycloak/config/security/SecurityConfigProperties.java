package com.ss.keycloak.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("app.security")
public record SecurityConfigProperties(Boolean enabled,
                                       String realm,
                                       String clientId,
                                       String secret,
                                       String authServerUrl,
                                       String authUrl,
                                       String tokenUrl,
                                       String refreshTokenUrl,
                                       List<String> corsOrigins) {
}
