package com.ss.keycloak.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RedisService {

    private final Map<String, List<String>> groupRoleMap = new HashMap<>();

    public void addNewGroup(String groupName, List<String> roles) {
        this.groupRoleMap.put(groupName, roles);
    }

    public List<String> getRolesFromGroupName(String groupName) {
        return this.groupRoleMap.get(groupName);
    }

    public List<String> getRolesFromGroupName(List<String> groupNames) {
        List<String> allRoles = new ArrayList<>();
        groupNames.forEach(e -> {
            List<String> roles = this.getRolesFromGroupName(e);
            if (roles != null && !roles.isEmpty()) {
                allRoles.addAll(roles);
            }
        });
        return allRoles;
    }

    public void addRolesToGroup(String groupName, List<String> newRoles) {
        List<String> roles = this.groupRoleMap.get(groupName);
        if (roles != null && !roles.isEmpty()) {
            roles.addAll(newRoles);
        }
        this.addNewGroup(groupName, roles);
    }

}
