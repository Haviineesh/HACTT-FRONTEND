package demo_ver.demo.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import demo_ver.demo.adapter.RoleAdapter;
import demo_ver.demo.model.ManageRole;

// Service class for managing roles
@Service
public class ManageRoleService {
    private static final Logger logger = LoggerFactory.getLogger(ManageRoleService.class);
    private static final String API_BASE_URL = "https://84cb-161-139-102-63.ngrok-free.app";

    private final RestTemplate restTemplate;
    private final RoleAdapter roleAdapter;

    @Autowired
    public ManageRoleService(RestTemplate restTemplate, RoleAdapter roleAdapter) {
        this.restTemplate = restTemplate;
        this.roleAdapter = roleAdapter;
    }

    public List<ManageRole> getAllRoles() {
        String url = API_BASE_URL + "/getAllRoles";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = new ObjectMapper().readTree(response.getBody());
                return roleAdapter.convertJsonToRoles(root);
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Error fetching roles from API: ", e);
            return Collections.emptyList();
        }
    }

    public ManageRole viewRoleById(String id) {
        String url = API_BASE_URL + "/getRole/" + id;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (response.getBody() != null) {
                return new ObjectMapper().readValue(response.getBody(), ManageRole.class);
            }
        } catch (Exception e) {
            logger.error("Error fetching role by ID: ", e);
        }
        return null;
    }

    public String createRole(String roleName, String description, String isActive) {
        String url = API_BASE_URL + "/createRole";

        String prefixedRoleName = addRolePrefix(roleName);

        // Check if role with same name already exists
        if (isRoleNameExists(prefixedRoleName)) {
            return "Role with roleName " + prefixedRoleName + " already exists.";
        }

        int newId = generateRoleId();
        ManageRole role = new ManageRole(newId, prefixedRoleName, description, isActive);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, role, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Role created successfully.";
            } else {
                return "Failed to create role.";
            }
        } catch (RestClientException e) {
            logger.error("Error creating role: ", e);
            return "Failed to create role: " + e.getMessage();
        }
    }

    public ResponseEntity<String> updateRole(ManageRole updatedRole) {
        String url = API_BASE_URL + "/updateRole";

        String prefixedRoleName = addRolePrefix(updatedRole.getRoleName());

        if (isOtherRoleNameExists(prefixedRoleName, updatedRole.getRoleID())) {
            return new ResponseEntity<>("Role with roleName " + prefixedRoleName + " already exists.",
                    HttpStatus.CONFLICT);
        }

        updatedRole.setRoleName(prefixedRoleName);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(updatedRole),
                    String.class);
            return response;
        } catch (RestClientException e) {
            logger.error("Error updating role: ", e);
            return new ResponseEntity<>("Failed to update role: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String deleteRole(String id) {
        String url = API_BASE_URL + "/deleteRole/" + id;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return "Role deleted successfully.";
            } else {
                return "Failed to delete role.";
            }
        } catch (RestClientException e) {
            logger.error("Error deleting role: ", e);
            return "Failed to delete role: " + e.getMessage();
        }
    }

    private int generateRoleId() {
        List<ManageRole> existingRoles = getAllRoles();
        return existingRoles.stream()
                .mapToInt(ManageRole::getRoleID)
                .max()
                .orElse(999) + 1;
    }

    public boolean isRoleNameExists(String roleName) {
        return getAllRoles().stream()
                .anyMatch(role -> role.getRoleName().equalsIgnoreCase(roleName));
    }

    private boolean isOtherRoleNameExists(String roleName, int currentRoleId) {
        return getAllRoles().stream()
                .anyMatch(role -> !Objects.equals(role.getRoleID(), currentRoleId)
                        && role.getRoleName().equalsIgnoreCase(roleName));
    }

    private String addRolePrefix(String roleName) {
        if (!roleName.startsWith("ROLE_")) {
            return "ROLE_" + roleName;
        }
        return roleName;
    }
}