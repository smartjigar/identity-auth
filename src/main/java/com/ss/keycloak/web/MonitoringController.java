package com.ss.keycloak.web;

import com.ss.keycloak.config.security.SecurityConfigProperties;
import com.ss.keycloak.service.RedisService;
import com.ss.keycloak.utils.constant.AppConstants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(AppConstants.APP_CONTEXT_PATH + "/monitoring")
public class MonitoringController {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private RedisService redisService;

    @Autowired
    private Keycloak keycloakClient;

    @Autowired
    private SecurityConfigProperties configProperties;

    @GetMapping("/endpoints")
    public ResponseEntity<List<String>> getEndpoints() {
        return new ResponseEntity<>(this.fetchRolesFromAPIs(), HttpStatus.OK);
    }

    @PutMapping("/role/keycloak")
    public void updateRoleKeycloak() {
        GroupsResource groups = this.keycloakClient.realm(this.configProperties.realm()).groups();
        for (GroupRepresentation groupRepresentation : groups.groups()) {
            GroupRepresentation gp = groups.group(groupRepresentation.getId()).toRepresentation();
            List<String> roles = new ArrayList<>();

            Map<String, List<String>> clientRole = gp.getClientRoles();
            for (String clientId : clientRole.keySet()) {
                roles.addAll(clientRole.get(clientId));
            }
            this.redisService.addNewGroup(gp.getName(), roles);
        }
    }

    private List<String> fetchRolesFromAPIs() {
        List<String> ls = new ArrayList<>();
        this.requestMappingHandlerMapping
                .getHandlerMethods()
                .keySet()
                .stream()
                .filter(p -> p.getActivePatternsCondition().toString().contains(AppConstants.APP_CONTEXT_PATH))
                .forEach(e -> ls.add(this.fetchRolesFromApis(e)));
        return ls;
    }

    private String fetchRolesFromApis(RequestMappingInfo rmInfo) {
        StringBuilder builder = new StringBuilder();
        if (!rmInfo.getMethodsCondition().isEmpty()) {
            Set<RequestMethod> httpMethods = rmInfo.getMethodsCondition().getMethods();
            builder.append(httpMethods.size() == 1 ? httpMethods.iterator().next().toString().toLowerCase() : httpMethods);
        }
        builder.append(rmInfo.getActivePatternsCondition());
        return builder.toString().replace("/", "_").replaceAll("\\[|\\]", "");
    }
}
