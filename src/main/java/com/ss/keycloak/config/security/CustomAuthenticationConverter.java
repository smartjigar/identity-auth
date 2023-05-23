package com.ss.keycloak.config.security;

import com.ss.keycloak.utils.constant.AppConstants;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter;
    private final String resourceId;

    public CustomAuthenticationConverter(String resourceId) {
        this.resourceId = resourceId;
        this.grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<GrantedAuthority> authorities = (this.grantedAuthoritiesConverter.convert(source))
                .stream()
                .collect(Collectors.toSet());
        authorities.addAll(this.extractResourceRoles(source, this.resourceId));
        return new JwtAuthenticationToken(source, authorities);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt, String resourceId) {
        //   Map<String, Object> resourceAccess = jwt.getClaim("realm_access");
        //Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(resourceId);
        List<String> groupNames = (ArrayList<String>) jwt.getClaim(AppConstants.CLAIMS_GROUP_NAME);
        if (groupNames != null) {
            return groupNames.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        } else {
            return Set.of();
        }
    }
}
