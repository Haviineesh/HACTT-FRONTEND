package demo_ver.demo.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import demo_ver.demo.model.TestPlan;
import demo_ver.demo.model.TestSuite;
import demo_ver.demo.model.Build;
import demo_ver.demo.service.TestPlanService;
import demo_ver.demo.service.TestSuiteService;
import demo_ver.demo.service.BuildService;

@Controller
public class TestPlanController {

    @Autowired
    private TestSuiteService testSuiteService;

    @Autowired
    private TestPlanService testPlanService;

    @Autowired
    private BuildService buildService;

    // Utility method to check if a user has a specific role
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase("ROLE_" + role));
    }

    // View the list of test plans
    @GetMapping("/viewTestPlans")
    public String viewTestPlans(Model model, Authentication authentication) {

        // Check if the current user has the 'STAKEHOLDER' role
        boolean isStakeholder = hasRole(authentication, "STAKEHOLDER");

        // Fetch all test plans
        // Fetch all test plans without parameters
        List<TestPlan> testPlans = testPlanService.viewTestPlans();
        model.addAttribute("testPlans", testPlans);

        // Add the role flag and test plans to the model
        model.addAttribute("testPlans", testPlans);
        model.addAttribute("isStakeholder", isStakeholder);

        return "viewTestPlans"; // Refers to viewTestPlans.html
    }

    @GetMapping("/createTestPlan")
    public String createTestPlan(Model model) {
        List<TestSuite> testSuites = testSuiteService.viewTestSuites();
        List<Build> builds = buildService.viewBuilds();

        model.addAttribute("testSuites", testSuites);
        model.addAttribute("builds", builds);
        return "createTestPlan"; // Refers to createTestPlan.html
    }

    // Create a test plan
    @PostMapping("/createTestPlan")
    public String createTestPlan(@RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) String isActive,
            @RequestParam(required = false) String isPublic,
            @RequestParam(required = false) List<String> assignedTestSuiteIDs,
            @RequestParam(required = false) String assignedBuildID,
            RedirectAttributes redirectAttributes) {

        String activeStatus = (isActive != null && !isActive.isEmpty()) ? isActive : "false";
        String publicStatus = (isPublic != null && !isPublic.isEmpty()) ? isPublic : "false";

        TestPlan testPlan = testPlanService.createTestPlan(name, description, activeStatus, publicStatus,
                assignedTestSuiteIDs, assignedBuildID);

        List<TestSuite> assignedTestSuites = new ArrayList<>();
        for (String testSuiteId : assignedTestSuiteIDs) {
            TestSuite testSuite = testSuiteService.viewTestSuiteById(testSuiteId);
            if (testSuite != null) {
                assignedTestSuites.add(testSuite);
            }
        }
        testPlan.setAssignedTestSuites(assignedTestSuites);

        redirectAttributes.addFlashAttribute("success", "Test plan created successfully");
        return "redirect:/viewTestPlans";
    }

    // Edit a test plan
    @GetMapping("/editTestPlan")
    public String editTestPlan(@RequestParam String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            TestPlan testPlan = testPlanService.viewTestPlanById(id);
            model.addAttribute("testPlan", testPlan);

            List<String> assignedTestSuiteIDs = testPlan.getAssignedTestSuite().stream()
                    .map(TestSuite::getTestSuiteID)
                    .collect(Collectors.toList());
            List<Build> builds = buildService.viewBuilds();

            model.addAttribute("assignedTestSuiteIDs", assignedTestSuiteIDs);
            model.addAttribute("builds", builds);
            List<TestSuite> testSuites = testSuiteService.viewTestSuites();
            model.addAttribute("testSuites", testSuites);
            return "editTestPlan";
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Test plan not found");
            return "redirect:/viewTestPlans";
        }
    }

    @PostMapping("/editTestPlan")
    public String updateTestPlan(@RequestParam String id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String createdBy,
            @RequestParam String dateCreated,
            @RequestParam(required = false) String isActive,
            @RequestParam(required = false) String isPublic,
            @RequestParam List<String> assignedTestSuiteIDs,
            @RequestParam String assignedBuildID,
            RedirectAttributes redirectAttributes) {

        String activeStatus = (isActive != null && !isActive.isEmpty()) ? isActive : "false";
        String publicStatus = (isPublic != null && !isPublic.isEmpty()) ? isPublic : "false";

        testPlanService.updateTestPlan(id, name, description, createdBy, dateCreated, activeStatus, publicStatus,
                assignedTestSuiteIDs, assignedBuildID, redirectAttributes);
        return "redirect:/viewTestPlans";
    }

    // Delete a test plan
    @PostMapping("/deleteTestPlan")
    public String deleteTestPlan(@RequestParam String id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = testPlanService.deleteTestPlan(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Test plan deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Test plan not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete test plan");
        }
        return "redirect:/viewTestPlans";
    }

    // View Test Plan Details
    @GetMapping("/viewTestPlanDetails/{id}")
    public String viewTestPlanDetails(@PathVariable("id") String id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            TestPlan testPlan = testPlanService.viewTestPlanById(id);
            Build build = buildService.viewBuildById(testPlan.getAssignedBuildID().getBuildID());
            List<TestSuite> assignedTestSuites = testPlan.getAssignedTestSuite().stream()
                    .map(testSuite -> testSuiteService.viewTestSuiteById(testSuite.getTestSuiteID()))
                    .collect(Collectors.toList());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isStakeholder = hasRole(authentication, "STAKEHOLDER");

            if (isStakeholder) {
                redirectAttributes.addFlashAttribute("error",
                        "You do not have permission to view detailed test plan information.");
                return "redirect:/viewTestPlans";
            }

            model.addAttribute("testPlan", testPlan);
            model.addAttribute("buildID", build.getBuildID());
            model.addAttribute("buildTitle", build.getBuildTitle());
            model.addAttribute("buildVersion", build.getBuildVersion());
            model.addAttribute("assignedTestSuites", assignedTestSuites);
            return "viewTestPlanDetails";

        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Test plan not found");
            return "redirect:/viewTestPlans";
        }
    }

}
