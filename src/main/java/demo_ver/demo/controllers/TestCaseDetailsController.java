package demo_ver.demo.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import demo_ver.demo.model.TestCase;
import demo_ver.demo.service.ViewCaseService;

@Controller
public class TestCaseDetailsController {

    @Autowired
    private ViewCaseService viewCaseService;

    @GetMapping("/testcases/details/{idtest_cases}")
    public String viewTestCaseDetails(@PathVariable("idtest_cases") Long idtest_cases, Model model, Principal principal,
            @AuthenticationPrincipal UserDetails userDetails) {
        TestCase testCase = viewCaseService.getTestCaseById(idtest_cases);
        model.addAttribute("testCase", testCase);

        List<Map<String, String>> testCaseSteps = new ArrayList<>();
        for (int i = 0; i < testCase.getTcSteps().size(); i++) {
            Map<String, String> step = new HashMap<>();
            step.put("step", testCase.getTcSteps().get(i));
            step.put("expectedResult", testCase.getExpectedResults().get(i));
            testCaseSteps.add(step);
        }
        model.addAttribute("testCaseSteps", testCaseSteps);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        boolean isTester = authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_Tester"));
        model.addAttribute("isTester", isTester);
        return "viewTestCasesDetails";
    }

    @GetMapping("/testcases/approveStatus/{idtest_cases}")
    public String approveTestCase(@PathVariable("idtest_cases") Long idtest_cases, Principal principal) {
        String username = principal.getName();
        viewCaseService.setUserStatusForTestCase(idtest_cases, username, "Approved");
        return "redirect:/view";
    }

    @PostMapping("/testcases/rejectStatus/{idtest_cases}")
    public String rejectTestCase(@PathVariable("idtest_cases") Long idtest_cases,
            @RequestParam String rejectionReason, Principal principal) {
        String username = principal.getName();
        viewCaseService.setUserStatusForTestCase(idtest_cases, username, "Rejected", rejectionReason);
        return "redirect:/view";
    }

    // @GetMapping("/testcases/needsRevisionStatus/{idtest_cases}")
    // public String needsRevisionTestCase(@PathVariable("idtest_cases") Long
    // idtest_cases, Principal principal) {
    // String username = principal.getName();
    // viewCaseService.setUserStatusForTestCase(idtest_cases, username, "Needs
    // Revision");
    // return "redirect:/view";
    // }

    // @GetMapping("/testcases/underReviewStatus/{idtest_cases}")
    // public String underReviewTestCase(@PathVariable("idtest_cases") Long
    // idtest_cases, Principal principal) {
    // String username = principal.getName();
    // viewCaseService.setUserStatusForTestCase(idtest_cases, username, "Under
    // Review");
    // return "redirect:/view";
    // }
}