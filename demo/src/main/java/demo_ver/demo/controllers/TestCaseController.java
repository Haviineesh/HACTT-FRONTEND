package demo_ver.demo.controllers;

import java.security.Principal; // Import Principal for getting logged-in user's information
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// import org.hyperledger.fabric.gateway.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;

// import demo_ver.demo.TestCaseRepository;
import demo_ver.demo.model.ManageUser;
import demo_ver.demo.model.TestCase;
import demo_ver.demo.service.ManageUserService;
import demo_ver.demo.service.ViewCaseService;

@Controller
public class TestCaseController {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ViewCaseService viewCaseService;

    @Autowired
    private ManageUserService manageUserService;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/view")
    public String viewCase(Model model, Principal principal, @AuthenticationPrincipal UserDetails userDetails)
            throws JsonProcessingException {
        List<TestCase> testCases = ViewCaseService.findAllList();

        // Assuming ManageUserService.getAllUsers() returns a List<ManageUser>
        List<ManageUser> allUsers = manageUserService.getAllUsers();
        String username = principal.getName();

        // Set username for each test case
        for (TestCase testCase : testCases) {
            List<Integer> userIds = testCase.getUserID();

            List<String> usernames = userIds.stream()
                    .map(userId -> {
                        ManageUser user = manageUserService.getUserById(userId);
                        return (user != null) ? user.getUsername() : "";
                    })
                    .collect(Collectors.toList());

            // Assuming you want to concatenate usernames into a single string
            testCase.setUsername(String.join(", ", usernames));

        }

        List<TestCase> userTestCases = viewCaseService.findTestCasesByUsername(username);

        model.addAttribute("testCase", userTestCases);
        model.addAttribute("users1", allUsers);
        // model.addAttribute("allTestCases", ViewCaseService.findAllList());
        // model.addAttribute("userTestCases",
        // viewCaseService.findTestCasesByUsername(username));
        // remove edit and delete if not tester
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        boolean isTester = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_Tester"));
        model.addAttribute("isTester", isTester);

        return "viewTestCase";
    }

    @GetMapping("/add")
    public String showAddTestCaseForm(Model model, Authentication authentication,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        model.addAttribute("testCase", new TestCase());
        model.addAttribute("users", manageUserService.getAllUsers());
        return "addTestCase";
    }

    @PostMapping("/save")
    public String addTestCaseForm(TestCase testCase, @RequestParam("userID") List<Integer> userID,
            @RequestParam List<String> tcSteps, @RequestParam List<String> expectedResults,
            @AuthenticationPrincipal UserDetails userDetails, Model model)
            throws JsonProcessingException {
        model.addAttribute("tests", ViewCaseService.findAllList());
        model.addAttribute("users", manageUserService.getAllUsers()); // I added this so that user list will always show
                                                                      // even if got validation errors

        // Check if the test case name already exists
        if (viewCaseService.istestCaseExists(testCase.getTestCaseName())) {
            model.addAttribute("testCaseNameExists", true);
            return "addTestCase";
        }
        // Check if the deadline is later than the date created
        if (!isDeadlineLaterThanDateCreated(testCase.getDateCreated(), testCase.getDeadline())) {
            model.addAttribute("deadlineInvalid", true);
            return "addTestCase";
        }

        List<Map<String, String>> testCaseSteps = new ArrayList<>();
        for (int i = 0; i < tcSteps.size(); i++) {
            Map<String, String> step = new HashMap<>();
            step.put("step", tcSteps.get(i));
            step.put("expectedResult", expectedResults.get(i));
            testCaseSteps.add(step);
        }
        // Proceed with adding the test case
        viewCaseService.addTestCaseForm(testCase, userID, userDetails.getUsername(), testCaseSteps);

        return "redirect:/view";
    }

    // Helper method to check if deadline is later than date created
    private boolean isDeadlineLaterThanDateCreated(String dateCreated, String deadline) {
        LocalDate createdDate = LocalDate.parse(dateCreated);
        LocalDate deadlineDate = LocalDate.parse(deadline);
        return deadlineDate.isAfter(createdDate);
    }

    @GetMapping("/deleteCase/{idtest_cases}")
    public String deleteCase(@PathVariable("idtest_cases") Long idtest_cases) {
        viewCaseService.deleteCase(idtest_cases);
        return "redirect:/view";
    }

    @GetMapping("/editCase/{idtest_cases}")
    public String editCase(@PathVariable("idtest_cases") Long idtest_cases, Model model) {
        TestCase testCaseToEdit = viewCaseService.getTestCaseById(idtest_cases);
        model.addAttribute("testCase", testCaseToEdit);
        model.addAttribute("users", manageUserService.getAllUsers());
        List<Map<String, String>> testCaseSteps = new ArrayList<>();
        for (int i = 0; i < testCaseToEdit.getTcSteps().size(); i++) {
            Map<String, String> step = new HashMap<>();
            step.put("step", testCaseToEdit.getTcSteps().get(i));
            step.put("expectedResult", testCaseToEdit.getExpectedResults().get(i));
            testCaseSteps.add(step);
        }
        model.addAttribute("testCaseSteps", testCaseSteps); // Add users for assigning to the test case
        return "EditTestCase"; // The name of the edit form template
    }

    @PostMapping("/update")
    public String editTestCaseForm(TestCase testCase, @RequestParam("userID") List<Integer> userID,
            @RequestParam(required = false) List<String> tcSteps,
            @RequestParam(required = false) List<String> expectedResults, Model model,
            Principal principal)
            throws JsonProcessingException {

        model.addAttribute("tests", ViewCaseService.findAllList());
        model.addAttribute("users", manageUserService.getAllUsers()); // I added this so that user list will always show
                                                                      // even if got validation errors
        String username = principal.getName(); // gets name of tester
        // if (viewCaseService.istestCaseExists(testCase.getTestCaseName())) {
        // model.addAttribute("testCaseNameExists", true);
        // return "EditTestCase";
        // }
        // Check if the deadline is later than the date created
        if (!isDeadlineLaterThanDateCreated(testCase.getDateCreated(), testCase.getDeadline())) {
            model.addAttribute("deadlineInvalid", true);
            return "EditTestCase";
        }
        List<Map<String, String>> testCaseSteps = new ArrayList<>();
        if (tcSteps != null && expectedResults != null && tcSteps.size() == expectedResults.size()) {
            for (int i = 0; i < tcSteps.size(); i++) {
                Map<String, String> step = new HashMap<>();
                step.put("step", tcSteps.get(i) != null ? tcSteps.get(i) : ""); // Default to empty string if null
                step.put("expectedResult", expectedResults.get(i) != null ? expectedResults.get(i) : ""); // Default to
                                                                                                          // empty
                                                                                                          // string if
                                                                                                          // null
                testCaseSteps.add(step);
            }
        }
        // pass testcase, userID, and testername
        viewCaseService.updateCaseUser(testCase, userID, username, testCaseSteps); // sends name of tester
        return "redirect:/view";
    }

    @PostMapping("/setUserStatus")
    public String setUserStatus(@RequestParam Long testCaseId, @RequestParam String status,
            @RequestParam(required = false) String rejectionReason, Principal principal) {
        String username = principal.getName(); // Get logged-in username
        viewCaseService.setUserStatusForTestCase(testCaseId, username, status, rejectionReason);
        return "redirect:/view";
    }
}