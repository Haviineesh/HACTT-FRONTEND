package demo_ver.demo.controllers;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import demo_ver.demo.model.Build;
import demo_ver.demo.service.BuildService;

@Controller
public class BuildController {

    @Autowired
    private BuildService buildService;

    // Utility method to check if a user has a specific role
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_" + role));
    }

    // View the list of builds
    // In BuildController.java
    @GetMapping("/viewBuilds")
    public String viewBuilds(@RequestParam(required = false) String filter,
            @RequestParam(required = false) String search,
            Model model, Authentication authentication) {

        // Check if the user has the STAKEHOLDER role
        boolean isStakeholder = hasRole(authentication, "STAKEHOLDER");

        // Fetch builds with filtering and searching
        List<Build> builds = buildService.viewBuilds(filter, search); // Call the service to get filtered builds

        // Add attributes to the model for the view
        model.addAttribute("builds", builds);
        model.addAttribute("isStakeholder", isStakeholder);
        model.addAttribute("filter", filter); // Add filter to the model
        model.addAttribute("search", search); // Add search to the model

        return "viewBuilds"; // Return the name of the view
    }

    // Create Build - GET
    @GetMapping("/createBuild")
    public String createBuildForm() {
        return "createBuild";
    }

    // Create Build - POST
    @PostMapping("/createBuild")
    public String createBuild(@RequestParam String buildTitle,
            @RequestParam String buildDescription,
            @RequestParam(required = false) String isBuildActive,
            @RequestParam(required = false) String isBuildOpen,
            @RequestParam String buildReleaseDate,
            @RequestParam String buildVersion,
            RedirectAttributes redirectAttributes) {

        String buildActiveStatus = (isBuildActive != null && !isBuildActive.isEmpty()) ? isBuildActive : "false";
        String buildOpenStatus = (isBuildOpen != null && !isBuildOpen.isEmpty()) ? isBuildOpen : "false";

        // Call the service method to create the build
        buildService.createBuild(buildTitle, buildDescription, buildActiveStatus, buildOpenStatus, buildReleaseDate,
                buildVersion);
        redirectAttributes.addFlashAttribute("success", "Build created successfully.");
        return "redirect:/viewBuilds"; // Redirect to the list of builds
    }

    // Edit Build - GET
    @GetMapping("/editBuild")
    public String editBuildForm(@RequestParam String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Build build = buildService.viewBuildById(id); // Ensure the ID exists
            model.addAttribute("build", build);
            return "editBuild"; // Pass the build to the view for editing
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Build not found.");
            return "redirect:/viewBuilds"; // Redirect if build not found
        }
    }

    // Edit Build - POST
    @PostMapping("/editBuild")
    public String updateBuild(@RequestParam String buildID,
            @RequestParam String buildTitle,
            @RequestParam String buildDescription,
            @RequestParam(required = false) String isBuildActive,
            @RequestParam(required = false) String isBuildOpen,
            @RequestParam String buildReleaseDate,
            @RequestParam String buildVersion,
            RedirectAttributes redirectAttributes) {

        // Default to "false" if null or empty
        String buildActiveStatus = (isBuildActive != null && !isBuildActive.isEmpty()) ? isBuildActive : "false";
        String buildOpenStatus = (isBuildOpen != null && !isBuildOpen.isEmpty()) ? isBuildOpen : "false";

        buildService.updateBuild(buildID, buildTitle, buildDescription, buildActiveStatus, buildOpenStatus,
                buildReleaseDate,
                buildVersion, redirectAttributes);
        return "redirect:/viewBuilds";// Redirect to the list of builds
    }

    // Delete Build
    @PostMapping("/deleteBuild")
    public String deleteBuild(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = buildService.deleteBuild(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Build deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Build not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete build");
        }
        return "redirect:/viewBuilds";
    }

    // View Build Details
    @GetMapping("/viewBuildDetails/{id}")
    public String viewBuildDetails(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Build build = buildService.viewBuildById(id);
            model.addAttribute("build", build);
            return "viewBuildDetails";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Build not found.");
            return "redirect:/viewBuilds";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred.");
            return "redirect:/viewBuilds";
        }
    }
}
