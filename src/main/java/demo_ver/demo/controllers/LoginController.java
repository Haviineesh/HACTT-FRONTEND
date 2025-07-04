package demo_ver.demo.controllers;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// Controller for handling login and home page requests
@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }

    @GetMapping("/home")
    public String showHomePage(Model model, Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Check if authentication or userDetails is null
        if (authentication == null || userDetails == null) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }

        String username = authentication.getName();
        model.addAttribute("username", username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Check if the user has the Admin role (case-sensitive)
        boolean isAdmin = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
        boolean isProjectManager = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_PROJECT_MANAGER"));

        // Log the role flags
        System.out.println("isAdmin: " + isAdmin);
        System.out.println("isProjectManager: " + isProjectManager);

        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isProjectManager", isProjectManager);
        return "HomePage";

    }
}
