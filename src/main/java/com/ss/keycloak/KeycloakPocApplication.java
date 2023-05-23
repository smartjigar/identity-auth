package com.ss.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.ss.keycloak")
@ConfigurationPropertiesScan
public class KeycloakPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeycloakPocApplication.class, args);
    }

}
