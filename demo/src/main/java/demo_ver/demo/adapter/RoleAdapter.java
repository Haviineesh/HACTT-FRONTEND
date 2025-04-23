package demo_ver.demo.adapter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import demo_ver.demo.model.ManageRole;

@Component
public class RoleAdapter {

    public List<ManageRole> convertJsonToRoles(JsonNode rootNode) {
        List<ManageRole> roles = new ArrayList<>();

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                ManageRole role = new ManageRole();

                role.setRoleID(node.path("roleId").asInt());
                role.setRoleName(node.path("roleName").asText());
                role.setDescription(node.path("description").asText());
                role.setIsActive(node.path("isActive").asText());

                roles.add(role);
            }
        }

        return roles;
    }
}