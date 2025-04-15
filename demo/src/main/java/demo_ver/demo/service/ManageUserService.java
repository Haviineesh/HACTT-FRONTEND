package demo_ver.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import demo_ver.demo.mail.MailService;
import demo_ver.demo.model.ManageUser;

@Service
public class ManageUserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private static List<ManageUser> userList;

    @Autowired
    private MailService mailService;

    @Autowired
    private final RestTemplate restTemplate;

    public ManageUserService(PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = new RestTemplate();
        initializeUserList();
        ManageRoleService roleService = new ManageRoleService(restTemplate);

    }

    // 1001 - Tester
    // 1002 - Project Manager
    // 1003 - Developer
    // 1004 - Stakeholder

    // Initialize the user list with some sample data
    private void initializeUserList() {
        userList = new ArrayList<>();
        userList.add(new ManageUser(2000, "teeneshsubramaniam10@gmail.com", "Teenesh", passwordEncoder.encode("123456"), // admin
                1000));
        userList.add(new ManageUser(2001, "user@gmail.com", "John", passwordEncoder.encode("123456"), 1002)); // Project Manager
        userList.add(
                new ManageUser(2002, "williamlik@graduate.utm.my", "Will", passwordEncoder.encode("123456"), 1001)); //Tester
                userList.add(
                new ManageUser(2004, "muhammadkasyfi@graduate.utm.my", "Kasyfi", passwordEncoder.encode("123456"), 1001)); //Tester
        userList.add(
                new ManageUser(2003, "Mahathir@gmail.com", "Mahathir", passwordEncoder.encode("123456"), 1003));
        userList.add(
                new ManageUser(2004, "williamlik@graduate.utm.my", "tester", passwordEncoder.encode("123456"), 1001));
        userList.add(new ManageUser(2005, "user@gmail.com", "manager", passwordEncoder.encode("123456"), 1002));

        // PTX members 2013, 2008, 2007
        userList.add(new ManageUser(2006, "user@gmail.com", "Fikri_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2007, "user@gmail.com", "Fikri_PM", passwordEncoder.encode("123456"), 1002)); // Project Manager
        userList.add(new ManageUser(2008, "user@gmail.com", "Iqmal_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2009, "user@gmail.com", "Fahmi_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2010, "user@gmail.com", "Ryan_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2011, "user@gmail.com", "Haziq_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2012, "user@gmail.com", "Kasyfi_Stake", passwordEncoder.encode("123456"), 1004)); // Stakeholder

        // // Plantfeed members 2016, 2017, 2019
        // userList.add(new ManageUser(2013, "user@gmail.com", "Omar_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        // userList.add(new ManageUser(2014, "user@gmail.com", "Omar_PM", passwordEncoder.encode("123456"), 1002)); // Project Manager
        // userList.add(new ManageUser(2015, "user@gmail.com", "Wang_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        // userList.add(new ManageUser(2016, "user@gmail.com", "Ali_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        // userList.add(new ManageUser(2017, "user@gmail.com", "Adila", passwordEncoder.encode("123456"), 1004)); // Stakeholder

        // APPS members
        // userList.add(new ManageUser(2018, "user@gmail.com", "Fathan_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        // userList.add(new ManageUser(2019, "user@gmail.com", "Iqbal_Developer", passwordEncoder.encode("123456"), 1003)); // Developer
        // userList.add(new ManageUser(2020, "user@gmail.com", "Altayeb_PM", passwordEncoder.encode("123456"), 1002)); // Project Manager

        // PowerHR
        userList.add(new ManageUser(2021, "user@gmail.com", "Kagi_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2022, "user@gmail.com", "Ain_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2023, "user@gmail.com", "Naim_PM", passwordEncoder.encode("123456"), 1002)); // Project Manager
        userList.add(new ManageUser(2024, "user@gmail.com", "Vinodh_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2025, "user@gmail.com", "Fazlin_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2026, "user@gmail.com", "Eikiesha_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2027, "user@gmail.com", "Izzah_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2028, "user@gmail.com", "Nadhirah", passwordEncoder.encode("123456"), 1004)); // Stakeholder

        // SAgile
        userList.add(new ManageUser(2029, "user@gmail.com", "Amar_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2030, "user@gmail.com", "Roshni_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2031, "user@gmail.com", "Amarul_PM", passwordEncoder.encode("123456"), 1002)); // Project Manager
        userList.add(new ManageUser(2032, "user@gmail.com", "Taufiq_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2033, "user@gmail.com", "Periyaa_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2034, "user@gmail.com", "Darwish_Tester", passwordEncoder.encode("123456"), 1001)); // Tester
        userList.add(new ManageUser(2035, "user@gmail.com", "Amar_Dev", passwordEncoder.encode("123456"), 1003)); // Developer
        userList.add(new ManageUser(2036, "user@gmail.com", "Havi", passwordEncoder.encode("123456"), 1004)); // Stakeholder

        
    }

    // Get all users in the system
    public static List<ManageUser> getAllUsers() {
        return userList;
    }

    // Add a new user to the system
    public void addUser(ManageUser newUser, int roleID) {
        if (isUserUnique(newUser)) {
            String plainTextPassword = newUser.getPassword(); // Get the plain text password before encoding
            newUser.setUserID(generateUserID());
            newUser.setRoleID(roleID);
            userList.add(newUser);

            // send email notification to new user with plain text password
            sendNewUserNotification(newUser, plainTextPassword);

            // Now, encode and set the password
            newUser.setPassword(passwordEncoder.encode(plainTextPassword));
        } else {
            // Handle duplicate user logic if needed
        }
    }

    // send email to new user
    private void sendNewUserNotification(ManageUser newUser, String plainTextPassword) {
        String subject = "Welcome to the System";
        String message = "Dear " + newUser.getUsername() + ",\n\n"
                + "Welcome to our system! Your account has been successfully created.\n"
                + "Username: " + newUser.getUsername() + "\n"
                + "Password: " + plainTextPassword + "\n"
                + "Please log in and change your password.\n\n"
                + "Best regards,\nThe System Team";

        mailService.sendAssignedMail(newUser.getEmail(), subject, message);
    }

    // Check if a user with the same username or email already exists
    private boolean isUserUnique(ManageUser newUser) {
        return userList.stream().noneMatch(user -> user.getUsername().equalsIgnoreCase(newUser.getUsername()) ||
                user.getEmail().equalsIgnoreCase(newUser.getEmail()));
    }

    // Delete a user by userID
    public void deleteUser(int userID) {
        userList.removeIf(user -> user.getUserID() == userID);
    }

    // Generate a new unique userID
    private int generateUserID() {
        return userList.get(userList.size() - 1).getUserID() + 1;
    }

    // Check if a username exists in the system
    public boolean isUsernameExists(String username) {
        return userList.stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(username));
    }

    // Check if an email exists in the system
    public boolean isEmailExists(String email) {
        return userList.stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    // Get a user by userID
    public static ManageUser getUserById(int userID) {
        return userList.stream()
                .filter(user -> user.getUserID() == userID)
                .findFirst()
                .orElse(null);
    }

    // Check if a username exists in the system excluding the current user
    public boolean isUsernameExistsExcludingCurrentUser(String username, int userID) {
        return userList.stream()
                .anyMatch(user -> user.getUserID() != userID && user.getUsername().equalsIgnoreCase(username));
    }

    // Check if an email exists in the system excluding the current user
    public boolean isEmailExistsExcludingCurrentUser(String email, int userID) {
        return userList.stream()
                .anyMatch(user -> user.getUserID() != userID && user.getEmail().equalsIgnoreCase(email));
    }

    // Update user details (including role)
    public void updateUser(ManageUser updatedUser, int roleID) {
        userList.stream()
                .filter(user -> user.getUserID() == updatedUser.getUserID())
                .findFirst()
                .ifPresent(user -> {
                    user.setEmail(updatedUser.getEmail());
                    user.setUsername(updatedUser.getUsername());
                    user.setRoleID(roleID);
                });
    }

    // Retrieve user details for authentication
    public ManageUser getUserByUsername(String username) {
        return userList.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // Load user details for authentication
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ManageUser manageUser = getUserByUsername(username);

        if (manageUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        ManageRoleService roleService = new ManageRoleService(restTemplate);
        List<GrantedAuthority> authorities = getAuthorities(roleService.apiFindByIdString(manageUser.getRoleID()));

        // Log user details and authorities
        // System.out.println("User Details: ");
        // System.out.println("Username: " + manageUser.getUsername());
        // System.out.println("Password: " + manageUser.getPassword());
        // System.out.println("Authorities: " + authorities);

        // Update the encoded password from ManageUser
        return new User(
                manageUser.getUsername(),
                manageUser.getPassword(), // Use the updated encoded password
                authorities);
    }

    // Get authorities for a user role
    private List<GrantedAuthority> getAuthorities(String role) {
        return new ArrayList<>(Collections.singletonList(new SimpleGrantedAuthority(role)));
    }

    // Check if a raw password matches an encoded password
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Update user password
    public void updateUserPassword(ManageUser user) {
        userList.stream()
                .filter(existingUser -> existingUser.getUserID() == user.getUserID())
                .findFirst()
                .ifPresent(existingUser -> existingUser.setPassword(passwordEncoder.encode(user.getPassword())));
    }

    // Find a user by reset token
    public ManageUser findUserByResetToken(String resetToken) {
        return userList.stream()
                .filter(user -> Objects.equals(user.getResetToken(), resetToken))
                .findFirst()
                .orElse(null);
    }

    // Update reset token for a user
    public void updateResetToken(ManageUser user, String resetToken) {
        user.setResetToken(resetToken);
    }

    // Check if a reset token is valid (dummy implementation)
    public boolean isValidToken(String token) {
        return true;
    }

    // Generate a reset token (dummy implementation)
    public String generateResetToken(String email) {
        return UUID.randomUUID().toString();
    }

    // Check if a password is valid
    public boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    // Update user password with a new password
    public void updateUserPassword(ManageUser user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    // Get a user by email
    public ManageUser getUserByEmail(String email) {
        return userList.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
}