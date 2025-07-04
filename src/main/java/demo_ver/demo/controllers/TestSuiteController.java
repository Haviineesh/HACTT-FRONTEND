package demo_ver.demo.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import demo_ver.demo.model.ManageUser;
import demo_ver.demo.model.TestPlan;
import demo_ver.demo.model.TestSuite;
import demo_ver.demo.service.ManageUserService;
import demo_ver.demo.service.TestSuiteService;
import demo_ver.demo.service.TestPlanService;

@Controller
public class TestSuiteController {

    @Autowired
    private TestSuiteService testSuiteService; // Injecting the service

    @Autowired
    private ManageUserService manageUserService; // Service to fetch user data

    @Autowired
    private TestPlanService testPlanService;

    // View the list of test suites
    @GetMapping("/viewTestSuites")
    public String viewTestSuites(Model model) {
        List<TestSuite> testSuites = testSuiteService.viewTestSuites();
        model.addAttribute("testSuites", testSuites);
        return "viewTestSuites"; // Refers to viewTestSuites.html
    }

    // Handle the "Create Test Suite" page
    @GetMapping("/createTestSuite")
    public String createTestSuite(Model model) {

        return "createTestSuite"; // Refers to createTestSuite.html
    }

    // Create a test suite
    @PostMapping("/createTestSuite")
    public String createTestSuite(@RequestParam String testSuiteName,
            @RequestParam String testSuiteDesc,
            RedirectAttributes redirectAttributes) {
        // Create the TestSuite
        TestSuite testSuite = testSuiteService.createTestSuite(testSuiteName, testSuiteDesc);

        redirectAttributes.addFlashAttribute("success", "Test suite created successfully");
        return "redirect:/viewTestSuites"; // Redirect to the viewTestSuites page after creation
    }

    // Edit a test suite
    @GetMapping("/editTestSuite")
    public String editTestSuite(@RequestParam String testSuiteID, Model model, RedirectAttributes redirectAttributes) {
        TestSuite testSuite;
        try {
            testSuite = testSuiteService.viewTestSuiteById(testSuiteID);
            model.addAttribute("testSuite", testSuite);
            List<TestPlan> testPlans = testPlanService.viewTestPlans();
            // Add the test plans to the model to be accessed in the Thymeleaf template
            model.addAttribute("testPlans", testPlans);
            return "editTestSuite"; // Return the name of the view (editTestSuite.html)
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Test suite not found");
            return "redirect:/viewTestSuites";
        }
    }

    // Update a test suite
    @PostMapping("/editTestSuite")
    public String updateTestSuite(@RequestParam String testSuiteID,
            @RequestParam String testSuiteName,
            @RequestParam String testSuiteDesc,
            @RequestParam String testSuiteStatus,
            @RequestParam String importance,
            @RequestParam String createdBy,
            @RequestParam String dateCreated,
            RedirectAttributes redirectAttributes) {
        try {
            testSuiteService.updateTestSuite(testSuiteID, testSuiteName, testSuiteDesc, testSuiteStatus, importance,
                    createdBy, dateCreated, redirectAttributes);
            redirectAttributes.addFlashAttribute("success", "Test suite updated successfully");
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Test suite not found");
        }
        return "redirect:/viewTestSuites";
    }

    // Delete a test suite
    @PostMapping("/deleteTestSuite")
    public String deleteTestSuite(@RequestParam String testSuiteID, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = testSuiteService.deleteTestSuite(testSuiteID);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Test suite deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Test suite not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete test suite");
        }
        return "redirect:/viewTestSuites"; // Redirect back to the test suites list
    }

    @GetMapping("/assignUsersToTestSuite")
    public String showAssignUsersForm(@RequestParam String testSuiteID, Model model) {
        TestSuite testSuite = testSuiteService.viewTestSuiteById(testSuiteID);
        List<ManageUser> allUsers = manageUserService.getAllUsers();

        model.addAttribute("testSuite", testSuite);
        model.addAttribute("users", allUsers); // Pass all users to the view
        return "assignUsersToTestSuite"; // Refers to assignUsersToTestSuite.html
    }

    // Assign users to a test suite
    @PostMapping("/assignUsersToTestSuite")
    public String assignUsersToTestSuite(@RequestParam String testSuiteID,
            @RequestParam List<Integer> userIds,
            RedirectAttributes redirectAttributes) {
        try {
            testSuiteService.assignUsersToTestSuite(testSuiteID, userIds);
            redirectAttributes.addFlashAttribute("success", "Users assigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to assign users");
        }
        return "redirect:/viewTestSuites"; // Redirect to test suites list
    }

    @GetMapping("/viewTestSuite")
    public String viewTestSuiteDetails(@RequestParam String testSuiteID, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            TestSuite testSuite = testSuiteService.viewTestSuiteById(testSuiteID);

            model.addAttribute("testSuite", testSuite);
            return "viewTestSuiteDetails"; // Refers to viewTestSuiteDetails.html
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", "Test suite not found");
            return "redirect:/viewTestSuites"; // Redirect back to the test suites list with an error message
        }
    }

}
