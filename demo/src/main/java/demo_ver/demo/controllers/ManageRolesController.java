package demo_ver.demo.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import demo_ver.demo.model.ManageRole;
import demo_ver.demo.service.ManageRoleService;

// Controller for handling role management requests
@Controller
public class ManageRolesController {
    @Autowired
    private ManageRoleService manageRoleService;

    public ManageRolesController(ManageRoleService manageRoleService) {
        this.manageRoleService = manageRoleService;
    }
    // @GetMapping("/manageroles")
    // @ResponseBody
    // public List<ManageRole> getAllRoles(){
    // return manageRoleService.getAllRoles();
    // }

    @GetMapping("/manageroles")
    public String getManageroles(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Check if the user has the Admin role
        boolean isAdmin = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_Admin"));

        boolean isProjectManager = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_Project_Manager"));

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isProjectManager", isProjectManager);
        model.addAttribute("roles", manageRoleService.getAllRoles());
        return "ManageRoles";
    }

    @GetMapping("/createrole")
    public String showCreateNewRole(Model model) {
        model.addAttribute("manageRole", new ManageRole());
        return "ManageRolesNew";
    }

    // @RequestMapping("/createrole")
    @PostMapping("/createrole")
    public String createRole(@ModelAttribute("manageRole") ManageRole manageRole, Model model) {
        if (manageRoleService.isRoleNameExists(manageRole.getRoleName())) {
            model.addAttribute("roleExists", true);
            return "ManageRolesNew";
        }

        manageRoleService.createRole(manageRole.getRoleName(), manageRole.getDescription(), "true");// save product into
                                                                                                    // database,
        // model.addAttribute("manageRole", manageRole);
        return "redirect:/manageroles";
    }

    @GetMapping("/editrole/{id}")
    public String editManagerole(@PathVariable("id") int id, Model model) {
        ManageRole role = manageRoleService.viewRoleById(String.valueOf(id));
        // Remove "ROLE_" if it exists in roleName
        String roleNameWithoutPrefix = role.getRoleName().startsWith("ROLE_") ? role.getRoleName().substring(5)
                : role.getRoleName();
        role.setRoleName(roleNameWithoutPrefix);
        model.addAttribute("role", role);

        return "ManageRolesEdit";
    }

    @PostMapping("/editrole")
    public String updateManageRole(ManageRole manageRole, Model model) {
        ResponseEntity<String> response = manageRoleService.updateRole(manageRole);

        if (response.getStatusCode().is2xxSuccessful()) {
            // Role updated successfully
            return "redirect:/manageroles"; // Redirect to the page where roles are managed
        } else {
            // Role update failed
            // You can handle the failure scenario here, such as displaying an error message
            model.addAttribute("errorMessage", "Failed to update role: " + response.getBody());
            return "errorPage"; // Redirect to an error page or return an error message
        }
    }

    @GetMapping("/deleterole/{id}")
    public String deleteRole(@PathVariable("id") int id) {
        manageRoleService.deleteRole(String.valueOf(id)); // Assuming you have a method to delete a role by id);
        return "redirect:/manageroles";
    }

}