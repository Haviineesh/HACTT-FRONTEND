package demo_ver.demo.adapter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

    public JsonNode convertRoleToJson(ManageRole role) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();

        node.put("roleId", String.valueOf(role.getRoleID()));
        node.put("roleName", role.getRoleName());
        node.put("description", role.getDescription());
        node.put("isActive", role.getIsActive());

        return node;
    }
}