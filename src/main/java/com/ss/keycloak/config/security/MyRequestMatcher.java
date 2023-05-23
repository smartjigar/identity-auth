package com.ss.keycloak.config.security;


import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.ss.keycloak.service.RedisService;
import com.ss.keycloak.utils.constant.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MyRequestMatcher implements RequestMatcher {
    @Autowired
    private RedisService redisService;

    @Autowired
    @Lazy
    private List<String> apiLists;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod().toLowerCase();
        path = path.replace("/", "_");
        String roleRequired = method + path;
        String token = request.getHeader(AppConstants.TOKEN_HEADER_NAME);
        String matchedAPi = null;

        for (String api : this.apiLists) {
            if (matches(roleRequired, api)) {
                matchedAPi = api.replaceAll("\\{|\\}", "-");
                break;
            }
        }

        if (matchedAPi != null && !StringUtils.isEmpty(token)) {
            JWTClaimsSet claimsSet = this.fetchClaimSetFromToken(token);
            if (claimsSet == null) {
                return false;
            }
            ArrayList groupsName = (ArrayList) claimsSet.getClaim(AppConstants.CLAIMS_GROUP_NAME);
            if (groupsName != null && groupsName.size() > 0) {
                List<String> redisRolesWithGroup = this.redisService.getRolesFromGroupName(groupsName);
                return redisRolesWithGroup.contains(matchedAPi);
            }
        }
        return false;
    }

    public static boolean matches(String requestUri, String actuatorEndpoint) {
        return PATH_MATCHER.match(actuatorEndpoint, requestUri);
    }

    private JWTClaimsSet fetchClaimSetFromToken(String token) {
        if (token.contains(AppConstants.TOKEN_PREFIX)) {
            token = token.substring(token.indexOf(" "));
        } else {
            return null;
        }
        try {
            JWT jwt = JWTParser.parse(token);
            return jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
