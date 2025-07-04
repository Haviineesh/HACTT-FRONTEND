package demo_ver.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import demo_ver.demo.adapter.TestPlanAdapter;
import demo_ver.demo.model.TestPlan;

@Service
public class TestPlanService {

    private static final Logger logger = LoggerFactory.getLogger(TestPlanService.class);
    private static final String HYPERLEDGER_BASE_URL = "https://562e-14-192-212-128.ngrok-free.app";
    private final TestPlanAdapter testPlanAdapter;

    @Autowired
    private TestSuiteService testSuiteService;

    @Autowired
    private BuildService buildService;

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    public TestPlanService(RestTemplate restTemplate, TestPlanAdapter testPlanAdapter) {
        this.restTemplate = restTemplate;
        this.testPlanAdapter = testPlanAdapter;
    }

    private List<TestPlan> testPlans = new ArrayList<>();

    // Create a test plan
    public TestPlan createTestPlan(String name, String testSuiteDesc, String isActive, String isPublic,
            List<String> assignedTestSuiteIDs, String assignedBuildID) {
        String id = generateTestPlanID();

        String activeStatus = (isActive != null && !isActive.isEmpty()) ? isActive : "false";
        String publicStatus = (isPublic != null && !isPublic.isEmpty()) ? isPublic : "false";

        String now = LocalDateTime.now().toString();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdBy = authentication.getName();

        TestPlan testPlan = new TestPlan(id.toString(), name, testSuiteDesc, activeStatus, publicStatus, createdBy,
                now);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tpID", testPlan.getTestPlanID());
            requestBody.put("tpName", testPlan.getTestPlanName());
            requestBody.put("tpDesc", testPlan.getDescription());
            requestBody.put("createdBy", testPlan.getCreatedBy());
            requestBody.put("dateCreated", testPlan.getDateCreated());
            requestBody.put("isActive", testPlan.getIsActive());
            requestBody.put("isPublic", testPlan.getIsPublic());
            requestBody.put("assignedTestSuiteIDs", assignedTestSuiteIDs);
            requestBody.put("assignedBuildID", assignedBuildID);

            String url = HYPERLEDGER_BASE_URL + "/createTestPlan";
            String response = restTemplate.postForObject(url, requestBody, String.class);
            logger.info("Test plan created successfully: {}", response);
        } catch (RestClientResponseException e) {
            logger.error("Error creating test plan:", e);
            throw new RuntimeException("Failed to create test plan", e);
        }

        return testPlan;
    }

    public String generateTestPlanID() {
        // Fetch the latest test plan ID from the API
        String latestTestPlanID = getLatestTestPlanIDFromAPI();

        // If no test plan exists, start from TP001
        if (latestTestPlanID == null || latestTestPlanID.isEmpty() || "No test plans found".equals(latestTestPlanID)) {
            return "TP001";
        }

        // Extract the numeric part from the latest test plan ID (e.g., "TP001" ->
        // "001")
        String numericPart = latestTestPlanID.substring(2); // Remove "TP" prefix
        int nextIdNumber = Integer.parseInt(numericPart) + 1; // Increment the number by 1

        // Format the new ID with the "TP" prefix and zero-padded number
        String formattedId = String.format("TP%03d", nextIdNumber);

        return formattedId;
    }

    // Helper method to get the latest test plan ID from the API
    private String getLatestTestPlanIDFromAPI() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = HYPERLEDGER_BASE_URL + "/getLatestTestPlanID"; // Replace with your actual API URL

            // Fetch the response as a JSON object (expects response like
            // {"latestTestPlanID": "TP001"})
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, Map.class);

            // Extract the latestTestPlanID from the response body
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("latestTestPlanID")) {
                return (String) responseBody.get("latestTestPlanID");
            } else {
                return "No test plans found"; // Return a default message if no ID is found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // If an error occurs, return null
        }
    }

    // View all test plans
    public List<TestPlan> viewTestPlans() {
        String url = HYPERLEDGER_BASE_URL + "/getAllTestPlans";

        try {
            // Call the Node.js API
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Check for empty or null response
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                throw new RuntimeException("Empty response from Node.js API");
            }

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Use the adapter to convert JSON to TestPlan objects
            return testPlanAdapter.convertJsonToTestPlans(rootNode);

        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API", e);
        } catch (Exception e) { // Catch generic Exception for other possible issues
            throw new RuntimeException("Error processing JSON response", e);
        }
    }

    public String updateTestPlan(String id, String name, String testSuiteDesc, String createdBy, String dateCreated,
            String isActive, String isPublic, List<String> assignedTestSuiteIDs, String assignedBuildID,
            RedirectAttributes redirectAttributes) {
        // Default to "false" if null or empty
        String activeStatus = (isActive != null && !isActive.isEmpty()) ? isActive : "false";
        String publicStatus = (isPublic != null && !isPublic.isEmpty()) ? isPublic : "false";
        TestPlan testPlan = new TestPlan();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedBy = authentication.getName();

        testPlan.setTestPlanID(id);
        testPlan.setTestPlanName(name);
        testPlan.setDescription(testSuiteDesc);
        testPlan.setIsActive(activeStatus);
        testPlan.setIsPublic(publicStatus);
        testPlan.setCreatedBy(createdBy);
        testPlan.setDateCreated(dateCreated);

        testPlan.setDateUpdated(LocalDateTime.now().toString());
        testPlan.setUpdatedBy(updatedBy);

        try {

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tpID", testPlan.getTestPlanID());
            requestBody.put("tpName", testPlan.getTestPlanName());
            requestBody.put("tpDesc", testPlan.getDescription());
            requestBody.put("createdBy", testPlan.getCreatedBy());
            requestBody.put("dateCreated", testPlan.getDateCreated());
            requestBody.put("isActive", testPlan.getIsActive());
            requestBody.put("isPublic", testPlan.getIsPublic());

            requestBody.put("updatedBy", testPlan.getUpdatedBy());
            requestBody.put("dateUpdated", testPlan.getDateUpdated());
            requestBody.put("assignedTestSuiteIDs", assignedTestSuiteIDs);
            requestBody.put("assignedBuildID", assignedBuildID);

            String updateUrl = HYPERLEDGER_BASE_URL + "/UpdateTestPlan";
            String log = restTemplate.postForObject(updateUrl, requestBody, String.class);
            logger.info("Test plan created successfully: {}", log);
            redirectAttributes.addFlashAttribute("success", "Test plan updated successfully");

        } catch (RestClientException e) {
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException httpException = (HttpClientErrorException) e;
                if (httpException.getStatusCode() == HttpStatus.NOT_FOUND) {
                    redirectAttributes.addFlashAttribute("error", "Test plan not found.");
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
        return "redirect:/viewTestPlans";
    }

    // Delete a test plan by ID
    public boolean deleteTestPlan(String id) {
        try {
            String url = HYPERLEDGER_BASE_URL + "/deleteTestPlan";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tpID", id);
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
    public TestPlan viewTestPlanById(String id) {
        String url = HYPERLEDGER_BASE_URL + "/getTestPlanById/" + id;
        try {
            // Call the Node.js API
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            TestPlan testPlan = objectMapper.convertValue(rootNode, TestPlan.class);

            return testPlan;
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }

    public List<TestPlan> filterAndSearchTestPlans(String filter, String search) {
        List<TestPlan> testPlans = viewTestPlans(); // Get all test plans

        // Filter based on the selected filter option
        if (filter != null) {
            switch (filter.toLowerCase()) {
                case "active":
                    testPlans = testPlans.stream()
                            .filter(testPlan -> "true".equalsIgnoreCase(testPlan.getIsActive())) // Filter for active
                                                                                                 // test plans
                            .collect(Collectors.toList());
                    break;
                case "open":
                    testPlans = testPlans.stream()
                            .filter(testPlan -> "true".equalsIgnoreCase(testPlan.getIsPublic())) // Filter for public
                                                                                                 // test plans
                            .collect(Collectors.toList());
                    break;
                case "all":
                default:
                    // No filtering needed, return all test plans
                    break;
            }
        }

        // Search by name
        if (search != null && !search.isEmpty()) {
            testPlans = testPlans.stream()
                    .filter(testPlan -> testPlan.getTestPlanName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return testPlans;
    }
}
