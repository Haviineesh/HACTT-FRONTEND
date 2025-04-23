package demo_ver.demo.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    public boolean createRole(String roleName, String description, String isActive) {
        String url = API_BASE_URL + "/createRole";

        int newId = generateRoleId();

        ManageRole role = new ManageRole(newId, roleName, description, isActive);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, role, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            logger.error("Error creating role: ", e);
            return false;
        }
    }

    public boolean updateRole(ManageRole role) {
        String url = API_BASE_URL + "/updateRole";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    new HttpEntity<>(role),
                    String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            logger.error("Error updating role: ", e);
            return false;
        }
    }

    public boolean deleteRole(String id) {
        String url = API_BASE_URL + "/deleteRole/" + id;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            logger.error("Error deleting role: ", e);
            return false;
        }
    }

    private int generateRoleId() {
        List<ManageRole> existingRoles = getAllRoles();
        return existingRoles.stream()
                .mapToInt(ManageRole::getRoleID)
                .max()
                .orElse(1000) + 1;
    }

    public boolean isRoleNameExists(String roleName) {
        return getAllRoles().stream()
                .anyMatch(role -> role.getRoleName().equalsIgnoreCase(roleName));
    }
}