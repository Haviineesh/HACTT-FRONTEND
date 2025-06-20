package demo_ver.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import demo_ver.demo.adapter.TestCaseAdapter;
import demo_ver.demo.mail.MailService;
import demo_ver.demo.model.ManageUser;
import demo_ver.demo.model.TestCase;
import demo_ver.demo.utils.RandomNumber;

@Service
public class ViewCaseService {
    private static final Logger logger = LoggerFactory.getLogger(ViewCaseService.class);
    private static final String API_BASE_URL = "https://dee1-113-211-96-19.ngrok-free.app";

    private final RestTemplate restTemplate;
    private final TestCaseAdapter testCaseAdapter;
    private final MailService mailService;
    private final ManageUserService manageUserService;

    @Autowired
    public ViewCaseService(RestTemplate restTemplate, TestCaseAdapter testCaseAdapter,
            MailService mailService, ManageUserService manageUserService) {
        this.restTemplate = restTemplate;
        this.testCaseAdapter = testCaseAdapter;
        this.mailService = mailService;
        this.manageUserService = manageUserService;
    }

    public List<TestCase> findAllList() {
        try {
            String url = API_BASE_URL + "/getAllTestCases";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody()).get("message");
            return testCaseAdapter.convertJsonToTestCases(root);
        } catch (Exception e) {
            logger.error("Error fetching test cases: ", e);
            return Collections.emptyList();
        }
    }

    public TestCase getTestCaseById(Long idtest_cases) {
        try {
            String url = API_BASE_URL + "/getTestCase/" + idtest_cases;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            JsonNode root = new ObjectMapper().readTree(response.getBody()).get("message");
            return testCaseAdapter.convertJsonToTestCase(root);
        } catch (Exception e) {
            logger.error("Error fetching test case by ID: ", e);
            return null;
        }
    }

    public TestCase addTestCaseForm(TestCase testCase, List<Integer> userID, String testerUsername,
            List<Map<String, String>> testCaseSteps) {
        testCase.setIdtest_cases(RandomNumber.getRandom(1, 1000));
        testCase.setUserID(userID);
        testCase.setOverallStatus("Pending");
        testCase.setUserStatus(testerUsername, "Approved");

        Map<String, Object> body = testCaseAdapter.convertTestCaseToJsonStringifiedFormat(testCase);

        try {
            String url = API_BASE_URL + "/createTestCase";
            restTemplate.postForObject(url, body, String.class);
        } catch (RestClientResponseException e) {
            logger.error("Error creating test case: ", e);
        }

        sendAssignmentNotification(testCase);
        scheduleDeadlineNotification(testCase);
        return testCase;
    }

    public void setUserStatusForTestCase(Long testCaseId, String username, String status) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase != null) {
            testCase.setUserStatus(username, status);
            String overallStatus = testCase.determineOverallStatus();
            testCase.setOverallStatus(overallStatus);
            updateCase(testCase);
        } else {
            throw new NoSuchElementException("Test case not found with ID: " + testCaseId);
        }
    }

    public void setUserStatusForTestCase(Long testCaseId, String username, String status, String rejectionReason) {
        TestCase testCase = getTestCaseById(testCaseId);
        if (testCase != null) {
            testCase.setUserStatus(username, status);
            if ("Rejected".equalsIgnoreCase(status)) {
                testCase.setUserReason(username, rejectionReason);
                // Send rejection email to all users in the test case
                for (Integer userId : testCase.getUserID()) {
                    ManageUser user = manageUserService.getUserById(userId);
                    if (user != null && user.getEmail() != null) {
                        String subject = "Test Case Rejected";
                        String message = String.format(
                                "Dear user,\n\nThe test case \"%s\" (ID: %s) has been rejected.\n\nReason by %s: %s\n\nPlease review the feedback and update accordingly.",
                                testCase.getTestCaseName(),
                                testCase.getIdtest_cases(),
                                username,
                                rejectionReason);
                        mailService.sendAssignedMail(user.getEmail(), subject, message);
                    }
                }
            }
            String overallStatus = testCase.determineOverallStatus();
            testCase.setOverallStatus(overallStatus);
            updateCase(testCase);
        } else {
            throw new NoSuchElementException("Test case not found with ID: " + testCaseId);
        }
    }

    private Optional<TestCase> findById(Long idtest_cases) {
        return Optional.ofNullable(getTestCaseById(idtest_cases));
    }

    public void updateCaseUser(TestCase updatedTestCase, List<Integer> userID, String testerUsername,
            List<Map<String, String>> testCaseSteps) {
        TestCase existingTestCase = getTestCaseById(updatedTestCase.getIdtest_cases());
        if (existingTestCase != null) {
            existingTestCase.setProjectId(updatedTestCase.getProjectId());
            existingTestCase.setTestCaseName(updatedTestCase.getTestCaseName());
            existingTestCase.setTest_desc(updatedTestCase.getTest_desc());
            existingTestCase.setDateCreated(updatedTestCase.getDateCreated());
            existingTestCase.setDeadline(updatedTestCase.getDeadline());
            existingTestCase.setUserID(userID);
            existingTestCase.resetUserStatuses();
            existingTestCase.setUserStatus(testerUsername, "Approved");

            List<String> steps = new ArrayList<>();
            List<String> expectedResults = new ArrayList<>();
            for (Map<String, String> step : testCaseSteps) {
                steps.add(step.getOrDefault("step", ""));
                expectedResults.add(step.getOrDefault("expectedResult", ""));
            }
            existingTestCase.setTcSteps(steps);
            existingTestCase.setExpectedResults(expectedResults);

            existingTestCase.setOverallStatus(existingTestCase.determineOverallStatus());
            updateCase(existingTestCase);
        } else {
            throw new NoSuchElementException("Test case not found with ID: " + updatedTestCase.getIdtest_cases());
        }
    }

    public void updateCase(TestCase testCase) {
        Map<String, Object> body = testCaseAdapter.convertTestCaseToJsonStringifiedFormat(testCase);

        try {
            String url = API_BASE_URL + "/updateTestCase";
            restTemplate.put(url, body);
        } catch (RestClientResponseException e) {
            logger.error("Error updating test case: ", e);
        }
        sendAssignmentNotification(testCase);
        scheduleDeadlineNotification(testCase);
    }

    public void deleteCase(Long idtest_cases) {
        try {
            String url = API_BASE_URL + "/deleteTestCase/" + idtest_cases;
            restTemplate.delete(url);
            logger.info("Test case deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting test case: ", e);
        }
    }

    public boolean istestCaseExists(String testCaseName) {
        return findAllList().stream().anyMatch(tc -> tc.getTestCaseName().equalsIgnoreCase(testCaseName));
    }

    public List<TestCase> findTestCasesByUsername(String username) {
        List<TestCase> result = new ArrayList<>();
        for (TestCase tc : findAllList()) {
            if (tc.getUsernames(manageUserService).contains(username)) {
                result.add(tc);
            }
        }
        return result;
    }

    private void sendAssignmentNotification(TestCase testCase) {
        for (Integer userId : testCase.getUserID()) {
            ManageUser user = manageUserService.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                String subject = "New Test Case Assignment";
                String message = String.format("Dear user,\nYou have been assigned a test case:\n" +
                        "ID: %s\nName: %s\nDeadline: %s\n\nPlease review before the deadline.",
                        testCase.getIdtest_cases(), testCase.getTestCaseName(), testCase.getDeadline());
                mailService.sendAssignedMail(user.getEmail(), subject, message);
            }
        }
    }

    private void scheduleDeadlineNotification(TestCase testCase) {
        LocalDateTime deadline = LocalDate.parse(testCase.getDeadline()).atStartOfDay();
        long delay = ChronoUnit.SECONDS.between(LocalDateTime.now(), deadline);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> sendDeadlineNotification(testCase), delay, TimeUnit.SECONDS);
        scheduler.schedule(scheduler::shutdown, delay + 3600, TimeUnit.SECONDS);
    }

    private void sendDeadlineNotification(TestCase testCase) {
        for (Integer userId : testCase.getUserID()) {
            ManageUser user = manageUserService.getUserById(userId);
            if (user != null && user.getEmail() != null) {
                String subject = "Test Case Deadline Reached";
                String message = String.format("Reminder:\nTest Case %s (%s) deadline has passed on %s.",
                        testCase.getIdtest_cases(), testCase.getTestCaseName(), testCase.getDeadline());
                mailService.sendAssignedMail(user.getEmail(), subject, message);
            }
        }
    }
}
