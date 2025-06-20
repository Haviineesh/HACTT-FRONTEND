package demo_ver.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import demo_ver.demo.model.TestPlan;
import demo_ver.demo.model.TestSuite;

@Service
public class TestSuiteService {

    private static final Logger logger = LoggerFactory.getLogger(TestSuiteService.class);
    private static final String HYPERLEDGER_BASE_URL = "https://1f1e-113-211-124-209.ngrok-free.app";

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TestPlanService testPlanService;

    public TestSuiteService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private List<TestSuite> testSuites = new ArrayList<>();

    public TestSuite createTestSuite(String testSuiteName, String testSuiteDesc) {
        String id = generateTestSuiteID();

        String now = LocalDateTime.now().toString();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        TestSuite testSuite = new TestSuite(id.toString(), testSuiteName, testSuiteDesc, createdBy, now);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tsID", testSuite.getTestSuiteID());
            requestBody.put("tsName", testSuite.getTestSuiteName());
            requestBody.put("tsDesc", testSuite.getTestSuiteDesc());
            requestBody.put("cb", testSuite.getCreatedBy());
            requestBody.put("dc", testSuite.getDateCreated());

            String url = HYPERLEDGER_BASE_URL + "/createTestSuite";
            String response = restTemplate.postForObject(url, requestBody, String.class);
            logger.info("Test suite created successfully: {}", response);
        } catch (RestClientResponseException e) {
            logger.error("Error creating test suite:", e);
            throw new RuntimeException("Failed to create test exam", e);
        }

        return testSuite;
    }

    public String generateTestSuiteID() {
        // Fetch the latest test plan ID from the API
        String latestTestSuiteID = getLatestTestSuiteIDFromAPI();

        // If no test plan exists, start from TP001
        if (latestTestSuiteID == null || latestTestSuiteID.isEmpty()
                || "No test plans found".equals(latestTestSuiteID)) {
            return "TS001";
        }

        // Extract the numeric part from the latest test plan ID (e.g., "TP001" ->
        // "001")
        String numericPart = latestTestSuiteID.substring(2); // Remove "TP" prefix
        int nextIdNumber = Integer.parseInt(numericPart) + 1; // Increment the number by 1

        // Format the new ID with the "TP" prefix and zero-padded number
        String formattedId = String.format("TS%03d", nextIdNumber);

        return formattedId;
    }

    // Helper method to get the latest test plan ID from the API
    private String getLatestTestSuiteIDFromAPI() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = HYPERLEDGER_BASE_URL + "/getLatestTestSuiteID"; // Replace with your actual API URL

            // Fetch the response as a JSON object (expects response like
            // {"latestTestPlanID": "TP001"})
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, Map.class);

            // Extract the latestTestPlanID from the response body
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("latestTestSuiteID")) {
                return (String) responseBody.get("latestTestSuiteID");
            } else {
                return "No test suites found"; // Return a default message if no ID is found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // If an error occurs, return null
        }
    }

    // View all test suites
    public List<TestSuite> viewTestSuites() {
        String url = HYPERLEDGER_BASE_URL + "/getAllTestSuites";

        try {
            // Call the Node.js API
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (rootNode.has("message")) {
                // Extract 'message' and map it to TestPlan[]
                TestSuite[] testSuiteArray = objectMapper.convertValue(
                        rootNode.get("message"),
                        TestSuite[].class);
                return Arrays.asList(testSuiteArray);
            } else {
                throw new RuntimeException("Invalid API response format");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }

    public String updateTestSuite(String testSuiteID, String testSuiteName, String testSuiteDesc,
            String testSuiteStatus, String importance,
            String createdBy, String dateCreated,
            RedirectAttributes redirectAttributes) {

        TestSuite testSuite = new TestSuite();

        testSuite.setId(testSuiteID);
        testSuite.setTestSuiteName(testSuiteName);
        testSuite.setTestSuiteDesc(testSuiteDesc);
        testSuite.setStatus(testSuiteStatus);
        testSuite.setImportance(importance);
        testSuite.setCreatedBy(createdBy);
        testSuite.setDateCreated(dateCreated);

        try {

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tsID", testSuite.getTestSuiteID());
            requestBody.put("tsName", testSuite.getTestSuiteName());
            requestBody.put("tsDesc", testSuite.getTestSuiteDesc());
            requestBody.put("tsStatus", testSuite.getTestSuiteStatus());
            requestBody.put("imp", testSuite.getImportance());
            requestBody.put("cb", testSuite.getCreatedBy());
            requestBody.put("dc", testSuite.getDateCreated());

            String updateUrl = HYPERLEDGER_BASE_URL + "/UpdateTestSuite";
            String log = restTemplate.postForObject(updateUrl, requestBody, String.class);
            logger.info("Test suite created successfully: {}", log);
            redirectAttributes.addFlashAttribute("success", "Test suite updated successfully");

        } catch (RestClientException e) {
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException httpException = (HttpClientErrorException) e;
                if (httpException.getStatusCode() == HttpStatus.NOT_FOUND) {
                    redirectAttributes.addFlashAttribute("error", "Test suite not found.");
                } else {
                    redirectAttributes.addFlashAttribute("error",
                            "Client error occurred: " + httpException.getMessage());
                }
            } else if (e instanceof HttpServerErrorException) {
                HttpServerErrorException serverException = (HttpServerErrorException) e;
                redirectAttributes.addFlashAttribute("error", "Server error occurred: " + serverException.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            }
        }
        return "redirect:/viewTestSuites";
    }

    public boolean deleteTestSuite(String id) {
        try {
            String url = HYPERLEDGER_BASE_URL + "/deleteTestSuite";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tsID", id);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity,
                    String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            }
        } catch (RestClientException e) {
            return false;
        }
        return false;
    }

    // View a test plan by ID
    public TestSuite viewTestSuiteById(String id) {
        String url = HYPERLEDGER_BASE_URL + "/getTestSuiteById/" + id;
        try {
            // Call the Node.js API
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            TestSuite testSuite = objectMapper.convertValue(rootNode, TestSuite.class);

            return testSuite;
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }

    // Assign users to a test suite
    public void assignUsersToTestSuite(String testSuiteId, List<Integer> userIds) {
        Optional<TestSuite> testSuiteOptional = testSuites.stream()
                .filter(suite -> suite.getTestSuiteID().equals(testSuiteId))
                .findFirst();

        if (testSuiteOptional.isPresent()) {
            TestSuite testSuite = testSuiteOptional.get();
            testSuite.setAssignedUserIds(userIds); // Assuming TestSuite has a field for assigned user IDs
        } else {
            throw new NoSuchElementException("Test suite not found with ID: " + testSuiteId);
        }
    }

    public TestSuite findById(Long testSuiteId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    public void save(TestSuite testSuite) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    // Assign test plans to a test suite
    /*
     * public void assignTestPlansToTestSuite(String testSuiteID, List<String>
     * testPlanIDs) {
     * // Retrieve the test suite by ID
     * Optional<TestSuite> testSuiteOptional = testSuites.stream()
     * .filter(suite -> suite.getTestSuiteID().equals(testSuiteID))
     * .findFirst();
     * 
     * if (testSuiteOptional.isPresent()) {
     * TestSuite testSuite = testSuiteOptional.get();
     * 
     * // Fetch the test plans and assign them to the test suite
     * List<TestPlan> assignedTestPlans = new ArrayList<>();
     * for (String testPlanId : testPlanIDs) {
     * TestPlan testPlan = findTestPlanById(testPlanId); // Fetch test plan from
     * some data source
     * if (testPlan != null) {
     * assignedTestPlans.add(testPlan);
     * } else {
     * throw new NoSuchElementException("Test plan not found with ID: " +
     * testPlanId);
     * }
     * }
     * 
     * // Assign the test plans to the test suite
     * testSuite.setAssignedTestPlans(assignedTestPlans);
     * 
     * // Send the updated test suite to the Hyperledger backend
     * try {
     * Map<String, Object> requestBody = new HashMap<>();
     * requestBody.put("testSuiteID", testSuite.getTestSuiteID());
     * requestBody.put("testPlanIDs", testPlanIDs);
     * 
     * String url = HYPERLEDGER_BASE_URL + "/assignTestPlansToTestSuite";
     * ResponseEntity<String> response = restTemplate.postForEntity(url,
     * requestBody, String.class);
     * 
     * if (response.getStatusCode() == HttpStatus.OK) {
     * logger.info("Successfully assigned test plans to test suite");
     * } else {
     * throw new RuntimeException("Failed to assign test plans: " +
     * response.getBody());
     * }
     * } catch (RestClientException e) {
     * logger.error("Error assigning test plans to test suite:", e);
     * throw new RuntimeException("Failed to assign test plans to test suite", e);
     * }
     * } else {
     * throw new NoSuchElementException("Test suite not found with ID: " +
     * testSuiteID);
     * }
     * }
     */

    private TestPlan findTestPlanById(String testPlanID) {
        String url = HYPERLEDGER_BASE_URL + "/getTestPlanById/" + testPlanID;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            TestPlan testPlan = objectMapper.readValue(response.getBody(), TestPlan.class);

            // Log the testPlanName to verify it's fetched correctly
            logger.info("Fetched Test Plan: {}", testPlan.getTestPlanName());

            return testPlan;
        } catch (RestClientException | JsonProcessingException e) {
            logger.error("Error fetching test plan by ID: {}", testPlanID, e);
            return null; // Return null or handle the error based on your use case
        }
    }

}
