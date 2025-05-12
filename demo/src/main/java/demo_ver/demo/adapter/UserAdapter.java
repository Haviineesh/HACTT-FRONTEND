package demo_ver.demo.adapter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import demo_ver.demo.model.ManageUser;

@Component
public class UserAdapter {

    public List<ManageUser> convertJsonToUsers(JsonNode rootNode) {
        List<ManageUser> users = new ArrayList<>();

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                ManageUser user = new ManageUser();

                user.setUserID(node.path("userId").asInt());
                user.setEmail(node.path("email").asText());
                user.setUsername(node.path("username").asText());
                user.setPassword(node.path("password").asText());
                user.setRoleID(node.path("roleId").asInt());
                user.setResetToken(node.path("resetToken").asText("")); // optional

                users.add(user);
            }
        }

        return users;
    }
}
