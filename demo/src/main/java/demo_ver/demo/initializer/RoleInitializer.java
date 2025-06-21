package demo_ver.demo.initializer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import demo_ver.demo.service.ManageRoleService;

@Component
public class RoleInitializer implements CommandLineRunner {

    private final ManageRoleService manageRoleService;

    public RoleInitializer(ManageRoleService manageRoleService) {
        this.manageRoleService = manageRoleService;
    }

    @Override
    public void run(String... args) {
        createRoleIfNotExists("Admin", "Administration");
        createRoleIfNotExists("Tester", "Unit Tester");
        createRoleIfNotExists("Product Manager", "Manager");
        createRoleIfNotExists("Developer", "Programming");
        createRoleIfNotExists("Stakeholder", "Holds Stakes");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        // Prefix "ROLE_" will be added inside the service method
        if (!manageRoleService.isRoleNameExists("ROLE_" + roleName)) {
            String response = manageRoleService.createRole(roleName, description, "true");
            System.out.println("Created role " + roleName + ": " + response);
        } else {
            System.out.println("Role " + roleName + " already exists.");
        }
    }
}
