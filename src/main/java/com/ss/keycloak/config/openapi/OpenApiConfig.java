package com.ss.keycloak.config.openapi;

import com.ss.keycloak.config.appinfo.InfoConfiguration;
import com.ss.keycloak.config.security.SecurityConfigProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@AllArgsConstructor
public class OpenApiConfig {

    private final SecurityConfigProperties properties;
    private final InfoConfiguration appInfoConfiguration;

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info();
        info.setTitle(this.appInfoConfiguration.name());
        info.setDescription(this.appInfoConfiguration.description());
        info.setVersion(this.appInfoConfiguration.version());
        OpenAPI openAPI = new OpenAPI();
        if (this.properties.enabled()) {
            openAPI = this.enableSecurity(openAPI);
        }
        return openAPI.info(info);
    }

    @Bean
    public GroupedOpenApi openApiDefinition() {
        return GroupedOpenApi.builder()
                .group("docs")
                .pathsToMatch("/**")
                .displayName("Docs")
                .build();
    }

    private OpenAPI enableSecurity(OpenAPI openAPI) {
        Components components = new Components();
        components.addSecuritySchemes(
                "open_id_scheme",
                new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows()
                                .authorizationCode(new OAuthFlow()
                                        .authorizationUrl(this.properties.authUrl())
                                        .tokenUrl(this.properties.tokenUrl())
                                        .refreshUrl(this.properties.refreshTokenUrl()
                                        )
                                )
                        )
        );
        return openAPI.components(components)
                .addSecurityItem(new SecurityRequirement().addList("open_id_scheme", Collections.emptyList()));
    }
}
