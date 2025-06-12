package demo_ver.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import demo_ver.demo.adapter.UserAdapter;
import demo_ver.demo.mail.MailService;
import demo_ver.demo.model.ManageUser;

@Service
public class ManageUserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(ManageUserService.class);
    private static final String API_BASE_URL = "https://4739-113-211-99-164.ngrok-free.app"; // Replace with your ngrok
                                                                                             // or actual API URL

    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final UserAdapter userAdapter;
    private final MailService mailService;

    @Autowired
    public ManageUserService(PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.userAdapter = new UserAdapter();
        this.mailService = new MailService();
    }

    @Autowired
    public ManageUserService(RestTemplate restTemplate, PasswordEncoder passwordEncoder, UserAdapter userAdapter,
            MailService mailService) {
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userAdapter = userAdapter;
        this.mailService = mailService;
    }

    public List<ManageUser> getAllUsers() {
        String url = API_BASE_URL + "/getAllUsers";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = new ObjectMapper().readTree(response.getBody());
                return userAdapter.convertJsonToUsers(root);
            }
        } catch (Exception e) {
            logger.error("Error fetching users: ", e);
        }
        return new ArrayList<>();
    }

    public void addUser(ManageUser newUser, int roleID) {
        if (isUserUnique(newUser)) {
            String plainTextPassword = newUser.getPassword();
            newUser.setUserID(generateUserID());
            newUser.setRoleID(roleID);
            newUser.setPassword(passwordEncoder.encode(plainTextPassword));

            String url = API_BASE_URL + "/createUser";
            try {
                JsonNode userJson = userAdapter.convertUserToJson(newUser);
                HttpEntity<JsonNode> request = new HttpEntity<>(userJson);
                restTemplate.postForEntity(url, request, String.class);
                sendNewUserNotification(newUser, plainTextPassword);
            } catch (RestClientException e) {
                logger.error("Error adding user: ", e);
            }
        }
    }

    private void sendNewUserNotification(ManageUser newUser, String plainTextPassword) {
        String subject = "Welcome to the System";
        String message = "Dear " + newUser.getUsername() + ",\n\n" +
                "Welcome to our system! Your account has been successfully created.\n" +
                "Username: " + newUser.getUsername() + "\n" +
                "Password: " + plainTextPassword + "\n" +
                "Please log in and change your password.\n\n" +
                "Best regards,\nThe System Team";

        mailService.sendAssignedMail(newUser.getEmail(), subject, message);
    }

    private boolean isUserUnique(ManageUser newUser) {
        return getAllUsers().stream().noneMatch(user -> user.getUsername().equalsIgnoreCase(newUser.getUsername()) ||
                user.getEmail().equalsIgnoreCase(newUser.getEmail()));
    }

    public void deleteUser(int userID) {
        String url = API_BASE_URL + "/deleteUser/" + userID;
        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            logger.error("Error deleting user: ", e);
        }
    }

    private int generateUserID() {
        List<ManageUser> users = getAllUsers();
        return users.stream().mapToInt(ManageUser::getUserID).max().orElse(1999) + 1;
    }

    public boolean isUsernameExists(String username) {
        return getAllUsers().stream()
                .anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
    }

    public boolean isEmailExists(String email) {
        return getAllUsers().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public ManageUser getUserById(int userID) {
        String url = API_BASE_URL + "/getUser/" + userID;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(response.getBody());

                ManageUser user = new ManageUser();
                user.setUserID(Integer.parseInt(node.path("userId").asText()));
                user.setEmail(node.path("email").asText());
                user.setUsername(node.path("username").asText());
                user.setPassword(node.path("password").asText());
                user.setRoleID(Integer.parseInt(node.path("roleId").asText()));
                user.setResetToken(node.path("resetToken").asText());

                return user;
            }
        } catch (Exception e) {
            logger.error("Error retrieving user by ID: ", e);
        }
        return null;
    }

    public boolean isUsernameExistsExcludingCurrentUser(String username, int userID) {
        return getAllUsers().stream()
                .anyMatch(user -> user.getUserID() != userID && user.getUsername().equalsIgnoreCase(username));
    }

    public boolean isEmailExistsExcludingCurrentUser(String email, int userID) {
        return getAllUsers().stream()
                .anyMatch(user -> user.getUserID() != userID && user.getEmail().equalsIgnoreCase(email));
    }

    public void updateUser(ManageUser updatedUser, int roleID) {
        updatedUser.setRoleID(roleID);
        String url = API_BASE_URL + "/updateUser";
        try {
            JsonNode userJson = userAdapter.convertUserToJson(updatedUser);
            HttpEntity<JsonNode> request = new HttpEntity<>(userJson);
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
        } catch (Exception e) {
            logger.error("Error updating user: ", e);
        }
    }

    public ManageUser getUserByUsername(String username) {
        return getAllUsers().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ManageUser manageUser = getUserByUsername(username);
        if (manageUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        ManageRoleService roleService = new ManageRoleService(restTemplate, null);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(
                roleService.viewRoleById(String.valueOf(manageUser.getRoleID())).getRoleName()));

        return new User(
                manageUser.getUsername(),
                manageUser.getPassword(),
                authorities);
    }

    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void updateUserPassword(ManageUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        updateUser(user, user.getRoleID());
    }

    public void updateUserPassword(ManageUser user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        updateUser(user, user.getRoleID());
    }

    public ManageUser findUserByResetToken(String resetToken) {
        return getAllUsers().stream()
                .filter(user -> Objects.equals(user.getResetToken(), resetToken))
                .findFirst()
                .orElse(null);
    }

    public void updateResetToken(ManageUser user, String resetToken) {
        user.setResetToken(resetToken);
        updateUser(user, user.getRoleID());
    }

    public boolean isValidToken(String token) {
        return true; // Simplified check for now
    }

    public String generateResetToken(String email) {
        return UUID.randomUUID().toString();
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public ManageUser getUserByEmail(String email) {
        return getAllUsers().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
}
