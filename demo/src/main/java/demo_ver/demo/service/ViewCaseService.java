package demo_ver.demo.service;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;

import demo_ver.demo.mail.MailService;
import demo_ver.demo.model.ManageUser;
import demo_ver.demo.model.TestCase;
import demo_ver.demo.utils.RandomNumber;

// service class for viewing test cases
@Service
public class ViewCaseService {
    private static final Logger logger = LoggerFactory.getLogger(ViewCaseService.class);

    // use this list to store test cases locally, switch to mysql or any database
    private static List<TestCase> testList = new ArrayList<TestCase>() {
        {
        }
    };

    @Autowired
    private MailService mailService;

    @Autowired
    private ManageUserService manageUserService;

    private static RestTemplate restTemplate = new RestTemplate();

    public ViewCaseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    // private static final String HYPERLEDGER_BASE_URL =
    // "http://172.20.228.232:3000"; // Use ngrok link here instead
    private static final String HYPERLEDGER_BASE_URL = "https://4739-113-211-99-164.ngrok-free.app"; // Use ngrok link
                                                                                                     // here insteada

    // private static final String HYPERLEDGER_BASE_URL =
    // "http://localhost:8090/api";

    // Get all test cases from hyperledger API
    public static List<TestCase> findAllList() throws JsonProcessingException {

        return testList;
    }

    // Add a new test case and send a POST request to the Hyperledger Fabric API
    public TestCase addTestCaseForm(TestCase testCase, List<Integer> userID, String testerUsername,
            List<Map<String, String>> testCaseSteps) {
        testCase.setIdtest_cases(RandomNumber.getRandom(1, 1000)); // TODO - generate a unique ID that wont be repeated
        testCase.setUserID(userID);
        testCase.setOverallStatus("Pending");
        testList.add(testCase);
        // incorrect method
        setUserStatusForTestCase(testCase.getIdtest_cases(), testerUsername, "Approved"); // IMPORTANT

        // Prepare the request body (assuming field names match API)
        try {
            Map<String, Object> requestBody = new HashMap<>();
            String idtestCasesString = testCase.getIdtest_cases().toString();
            requestBody.put("id", idtestCasesString);
            requestBody.put("pid", testCase.getProjectId());
            requestBody.put("tcn", testCase.getTestCaseName());
            requestBody.put("tcdesc", testCase.getTest_desc());
            requestBody.put("dtc", testCase.getDateCreated());
            requestBody.put("dl", testCase.getDeadline());
            // int firstUserID = userID.stream().findFirst().orElse(0);
            // // convert userID to String
            // String userIDString = Integer.toString(firstUserID);
            // requestBody.put("uid", userIDString);
            requestBody.put("ostts", testCase.getOverallStatus());
            requestBody.put("usrn", testerUsername);
            requestBody.put("tcSteps", testCaseSteps);
            // requestBody.put("crtdby", testCase.getCreatedBy());
            // requestBody.put("stts", testCase.getStatus());

            // Send POST request using RestTemplate
            String url = HYPERLEDGER_BASE_URL + "/createTestCase"; // Replace with your API URL
            String response = restTemplate.postForObject(url, requestBody, String.class);
        } catch (RestClientResponseException e) {
            // Handle potential errors from the API call
            logger.error("Error creating test case:", e);
            // You can potentially handle specific error codes or exceptions here
        }

        sendAssignmentNotification(testCase);
        scheduleDeadlineNotification(testCase);
        return testCase;
    }

    // send email notification to assigned users
    private void sendAssignmentNotification(TestCase testCase) {
        List<Integer> assignedUserIDs = testCase.getUserID();
        for (Integer userID : assignedUserIDs) {
            ManageUser user = manageUserService.getUserById(userID); 
            if (user != null && user.getEmail() != null) {
                String userEmail = user.getEmail();
                String subject = "New Test Case Assignment";
                String message = "Dear user, you have been assigned a new test case. Details:\n" +
                        "Test Case ID: " + testCase.getIdtest_cases() + "\n" +
                        "Test Case Name: " + testCase.getTestCaseName() + "\n" +
                        "Deadline: " + testCase.getDeadline() + "\n" +
                        "Please review and approve the test case before the deadline.";
                mailService.sendAssignedMail(userEmail, subject, message);
            }
        }
    }

    private void scheduleDeadlineNotification(TestCase testCase) {
        LocalDateTime deadlineDateTime = LocalDate.parse(testCase.getDeadline()).atStartOfDay();
        long initialDelay = ChronoUnit.SECONDS.between(LocalDateTime.now(), deadlineDateTime);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleWithFixedDelay(() -> sendDeadlineNotification(testCase), initialDelay, 24 * 60 * 60,
                TimeUnit.SECONDS);
        executorService.schedule(() -> executorService.shutdown(), initialDelay + 24 * 60 * 60, TimeUnit.SECONDS);
    }

    private void sendDeadlineNotification(TestCase testCase) {
        List<Integer> assignedUserIDs = testCase.getUserID();
        for (Integer userID : assignedUserIDs) {
            ManageUser user = manageUserService.getUserById(userID);
            if (user != null && user.getEmail() != null) {
                String userEmail = user.getEmail();
                String subject = "Test Case Deadline Notification";
                String message = "Dear user, the deadline for the assigned test case has been reached. Details:\n" +
                        "Test Case ID: " + testCase.getIdtest_cases() + "\n" +
                        "Test Case Name: " + testCase.getTestCaseName() + "\n" +
                        "Deadline: " + testCase.getDeadline() + "\n" +
                        "Please ensure that the test case is completed.";
                mailService.sendAssignedMail(userEmail, subject, message);
            }
        }
    }

    // Update the status of a test case for a specific user
    public void setUserStatusForTestCase(Long testCaseId, String username, String status) {
        Optional<TestCase> testCaseOptional = findById(testCaseId);
        if (testCaseOptional.isPresent()) {
            TestCase testCase = testCaseOptional.get();
            testCase.setUserStatus(username, status);
            String overallStatus = testCase.determineOverallStatus(); // Determine the overall status
            // Assuming you have a method setOverallStatus in your TestCase model
            testCase.setOverallStatus(overallStatus); // Update the overall status
            updateCase(testCase);
        } else {
            throw new NoSuchElementException("Test case not found with ID: " + testCaseId);
        }
    }

    // Update the status of a test case for a specific user
    public void setUserStatusForTestCase(Long testCaseId, String username, String status, String rejectionReason) {
        Optional<TestCase> testCaseOptional = findById(testCaseId);
        if (testCaseOptional.isPresent()) {
            TestCase testCase = testCaseOptional.get();
            testCase.setUserStatus(username, status);
            // Determine the overall status
            if ("Rejected".equals(status)) {
                testCase.setUserReason(username, rejectionReason);
            }
            String overallStatus = testCase.determineOverallStatus();
            testCase.setOverallStatus(overallStatus);
            updateCase(testCase);
        } else {
            throw new NoSuchElementException("Test case not found with ID: " + testCaseId);
        }
    }

    private Optional<TestCase> findById(Long idtest_cases) {
        return testList.stream()
                .filter(t -> t.getIdtest_cases() == idtest_cases.longValue())
                .findFirst();
    }

    // Set the status of a test case to approved for a tester and send a POST
    // request to the Hyperledger Fabric API
    public void updateCaseUser(TestCase updatedTestCase, List<Integer> userID, String testerUsername,
            List<Map<String, String>> testCaseSteps) {
        Optional<TestCase> existingTestCaseOpt = findById(updatedTestCase.getIdtest_cases());
        if (existingTestCaseOpt.isPresent()) {
            TestCase existingTestCase = existingTestCaseOpt.get();
            existingTestCase.setProjectId(updatedTestCase.getProjectId());
            // existingTestCase.setSmartContractID(updatedTestCase.getSmartContractID());
            existingTestCase.setTestCaseName(updatedTestCase.getTestCaseName());
            existingTestCase.setTest_desc(updatedTestCase.getTest_desc());
            existingTestCase.setDateCreated(updatedTestCase.getDateCreated());
            existingTestCase.setDeadline(updatedTestCase.getDeadline());
            existingTestCase.setUserID(userID);
            // Here, you might also want to update the user statuses if necessary
            // existingTestCase.setUserStatuses(updatedTestCase.getUserStatuses());
            existingTestCase.resetUserStatuses();
            existingTestCase.setUserStatus(testerUsername, "Approved"); // set tester as approved

            // Clear existing steps and expected results
            List<String> steps = new ArrayList<>();
            List<String> expectedResults = new ArrayList<>();

            // Populate the steps and expected results from the input
            if (testCaseSteps != null && !testCaseSteps.isEmpty()) {
                for (Map<String, String> step : testCaseSteps) {
                    steps.add(step.getOrDefault("step", "")); // Default to an empty string if null
                    expectedResults.add(step.getOrDefault("expectedResult", "")); // Default to an empty string if null
                }
            }

            // Set the new steps and expected results
            existingTestCase.setTcSteps(steps);
            existingTestCase.setExpectedResults(expectedResults);

            String overallStatus = existingTestCase.determineOverallStatus(); // Recalculate overall status
            existingTestCase.setOverallStatus(overallStatus);

            try {
                // Prepare the request body (assuming field names match API)
                Map<String, Object> requestBody = new HashMap<>();
                String idtestCasesString = updatedTestCase.getIdtest_cases().toString();
                requestBody.put("id", idtestCasesString);
                requestBody.put("pid", updatedTestCase.getProjectId());
                requestBody.put("tcn", updatedTestCase.getTestCaseName());
                requestBody.put("tcdesc", updatedTestCase.getTest_desc());
                requestBody.put("dtc", updatedTestCase.getDateCreated());
                requestBody.put("dl", updatedTestCase.getDeadline());
                requestBody.put("usrn", testerUsername);
                requestBody.put("ostts", existingTestCase.getOverallStatus());
                requestBody.put("tcSteps", testCaseSteps);

                // Send POST request using RestTemplate
                String url = HYPERLEDGER_BASE_URL + "/updateTestCase"; // Replace with your API URL
                String response = restTemplate.postForObject(url, requestBody, String.class);
            } catch (RestClientResponseException e) {
                // Handle potential errors from the API call
                logger.error("Error updating test case:", e);
                // You can potentially handle specific error codes or exceptions here
            }
        } else {
            throw new NoSuchElementException("Test case not found with ID: " + updatedTestCase.getIdtest_cases());
        }
    }

    // Update the status of a test case for a specific user and send a POST request
    // to the Hyperledger Fabric API if overall status is Approved or Rejected
    private void updateCase(TestCase testCase) {
        // deleteCase(testCase.getIdtest_cases());
        Long idtest_cases = testCase.getIdtest_cases();
        testList.removeIf(t -> t.getIdtest_cases() == idtest_cases);
        testList.add(testCase);
        if (testCase.getOverallStatus().equals("Approved")) {
            try {
                // Prepare the request body (assuming field names match API)
                Map<String, Object> requestBody = new HashMap<>();
                String idtestCasesString = testCase.getIdtest_cases().toString();
                requestBody.put("id", idtestCasesString);
                requestBody.put("pid", testCase.getProjectId());
                requestBody.put("tcn", testCase.getTestCaseName());
                requestBody.put("tcdesc", testCase.getTest_desc());
                requestBody.put("dtc", testCase.getDateCreated());
                requestBody.put("dl", testCase.getDeadline());
                requestBody.put("usrn", testCase.getUsername());
                requestBody.put("ostts", testCase.getOverallStatus());

                // Send POST request using RestTemplate
                String url = HYPERLEDGER_BASE_URL + "/updateTestCase"; // Replace with your API URL
                String response = restTemplate.postForObject(url, requestBody, String.class);

            } catch (RestClientResponseException e) {
                // Handle potential errors from the API call
                logger.error("Error updating test case:", e);
                // You can potentially handle specific error codes or exceptions here
            }
        } else if (testCase.getOverallStatus().equals("Rejected")) {
            try {
                // Prepare the request body (assuming field names match API)
                Map<String, Object> requestBody = new HashMap<>();
                String idtestCasesString = testCase.getIdtest_cases().toString();
                requestBody.put("id", idtestCasesString);
                requestBody.put("pid", testCase.getProjectId());
                requestBody.put("tcn", testCase.getTestCaseName());
                requestBody.put("tcdesc", testCase.getTest_desc());
                requestBody.put("dtc", testCase.getDateCreated());
                requestBody.put("dl", testCase.getDeadline());
                requestBody.put("usrn", testCase.getUsername());
                requestBody.put("ostts", testCase.getOverallStatus());

                // Send POST request using RestTemplate
                String url = HYPERLEDGER_BASE_URL + "/updateTestCase"; // Replace with your API URL
                String response = restTemplate.postForObject(url, requestBody, String.class);

            } catch (RestClientResponseException e) {
                // Handle potential errors from the API call
                logger.error("Error updating test case:", e);
                // You can potentially handle specific error codes or exceptions here
            }
        }

    }

    // Delete a test case and send a DELETE request to the Hyperledger Fabric API
    public void deleteCase(Long idtest_cases) {
        String idtestCasesString = idtest_cases.toString();
        String requestBody = "" + idtestCasesString; // Simple string concatenation
        logger.info("Request body: {}", requestBody);

        String url = HYPERLEDGER_BASE_URL + "/deleteTestCase";
        try {
            restTemplate.delete(url, requestBody);
            // Success scenario (optional)
            logger.info("Test case deleted successfully from Hyperledger Fabric");
        } catch (RestClientResponseException e) {
            // Handle potential errors from the API call
            logger.error("Error deleting test case:", e);
            // You can potentially handle specific error codes or exceptions here
        }
        // Remove the test case from the local list only if deletion on Hyperledger
        // Fabric was successful (optional)
        testList.removeIf(t -> t.getIdtest_cases() == idtest_cases);
    }

    // Check if a username exists in the system
    public boolean istestCaseExists(String testCaseName) {
        return testList.stream().anyMatch(Test -> Test.getTestCaseName().equalsIgnoreCase(testCaseName));
    }

    // In ViewCaseService class

    public TestCase getTestCaseById(Long idtest_cases) {
        return testList.stream()
                .filter(testCase -> testCase.getIdtest_cases().equals(idtest_cases))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Test case not found with ID: " + idtest_cases));
    }

    public List<TestCase> findTestCasesByUsername(String username) {
        List<TestCase> userTestCases = new ArrayList<>();

        for (TestCase testCase : testList) {
            if (testCase.getUsernames(manageUserService).contains(username)) {
                userTestCases.add(testCase);
            }
        }

        return userTestCases;
    }

}