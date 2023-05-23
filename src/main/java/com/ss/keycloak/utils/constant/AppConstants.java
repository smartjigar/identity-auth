package com.ss.keycloak.utils.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppConstants {

    public static final String ERROR = "error";
    public static final String APP_CONTEXT_PATH = "/api/app";
    public static final String CLAIMS_GROUP_NAME = "groups_name";
    public static final String TOKEN_HEADER_NAME = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer";
}
