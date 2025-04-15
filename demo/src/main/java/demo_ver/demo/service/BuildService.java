package demo_ver.demo.service;

import demo_ver.demo.model.Build;
import demo_ver.demo.model.TestPlan;

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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

@Service
public class BuildService {

    private static final Logger logger = LoggerFactory.getLogger(BuildService.class);
    private static final String HYPERLEDGER_BASE_URL = "https://8ece-14-192-212-187.ngrok-free.app";

    @Autowired
    private RestTemplate restTemplate = new RestTemplate();

    public BuildService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Create a new build with specific parameters
    public Build createBuild(String buildTitle, String buildDescription, String isBuildActive,
            String isBuildOpen, String buildReleaseDate, String buildVersion) {

        String id = generateBuildID();

        String buildActiveStatus = (isBuildActive != null && !isBuildActive.isEmpty()) ? isBuildActive : "false";
        String buildOpenStatus = (isBuildOpen != null && !isBuildOpen.isEmpty()) ? isBuildOpen : "false";

        Build build = new Build(id.toString(), buildTitle, buildDescription, buildActiveStatus,
                buildOpenStatus, buildReleaseDate, buildVersion);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bId", build.getBuildID());
            requestBody.put("bTitle", build.getBuildTitle());
            requestBody.put("bDesc", build.getBuildDescription());
            requestBody.put("bActive", build.getIsBuildActive());
            requestBody.put("bOpen", build.getIsBuildOpen());
            requestBody.put("bReleaseDate", build.getBuildReleaseDate());
            requestBody.put("bVersion", build.getBuildVersion());

            String url = HYPERLEDGER_BASE_URL + "/createBuild";
            String response = restTemplate.postForObject(url, requestBody, String.class);
            logger.info("Build created successfully: {}", response);
        } catch (RestClientResponseException e) {
            logger.error("Error creating build:", e);
            throw new RuntimeException("Failed to create bulild", e);
        }

        return build;
    }

    public String generateBuildID() {
        // Fetch the latest test plan ID from the API
        String latestBuildID = getLatestBuildIDFromAPI();

        // If no test plan exists, start from TP001
        if (latestBuildID == null || latestBuildID.isEmpty() || "No builds found".equals(latestBuildID)) {
            return "B001";
        }

        // Extract the numeric part from the latest test plan ID (e.g., "TP001" ->
        // "001")
        String numericPart = latestBuildID.substring(2); // Remove "TP" prefix
        int nextIdNumber = Integer.parseInt(numericPart) + 1; // Increment the number by 1

        // Format the new ID with the "TP" prefix and zero-padded number
        String formattedId = String.format("B%03d", nextIdNumber);

        return formattedId;
    }

    // Helper method to get the latest test plan ID from the API
    private String getLatestBuildIDFromAPI() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = HYPERLEDGER_BASE_URL + "/getLatestBuildID"; // Replace with your actual API URL

            // Fetch the response as a JSON object (expects response like
            // {"latestTestPlanID": "TP001"})
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, Map.class);

            // Extract the latestTestPlanID from the response body
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("latestBuildID")) {
                return (String) responseBody.get("latestBuildID");
            } else {
                return "No builds found"; // Return a default message if no ID is found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // If an error occurs, return null
        }
    }

    // view all builds
    public List<Build> viewBuilds() {
        String url = HYPERLEDGER_BASE_URL + "/getAllBuilds";

        try {
            // Call the Node.js API
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (rootNode.has("message")) {
                // Extract 'message' and map it to TestPlan[]
                Build[] buildArray = objectMapper.convertValue(
                        rootNode.get("message"),
                        Build[].class);
                return Arrays.asList(buildArray);
            } else {
                throw new RuntimeException("Invalid API response format");
            }
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }

    // View a build by ID
    public Build viewBuildById(String id) {
        String url = HYPERLEDGER_BASE_URL + "/getBuildById/" + id;
        try {
            // Call the Node.js API
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            Build build = objectMapper.convertValue(rootNode, Build.class);

            return build;
        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API", e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON response", e);
        }
    }

    // Delete a build by ID
    public boolean deleteBuild(String id) {
        try {
            String url = HYPERLEDGER_BASE_URL + "/deleteBuild";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bId", id);
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

    // Update an existing build by ID
    public String updateBuild(String buildID, String buildTitle, String buildDescription,
            String isBuildActive, String isBuildOpen, String buildReleaseDate, String buildVersion,
            RedirectAttributes redirectAttributes) {

        String buildActiveStatus = (isBuildActive != null && !isBuildActive.isEmpty()) ? isBuildActive : "false";
        String buildOpenStatus = (isBuildOpen != null && !isBuildOpen.isEmpty()) ? isBuildOpen : "false";

        Build build = new Build();

        build.setBuildID(buildID);
        build.setBuildTitle(buildTitle);
        build.setBuildDescription(buildDescription);
        build.setIsBuildActive(buildActiveStatus);
        build.setIsBuildOpen(buildOpenStatus);
        build.setBuildReleaseDate(buildReleaseDate);
        build.setBuildVersion(buildVersion);

        try {

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("bId", build.getBuildID());
            requestBody.put("bTitle", build.getBuildTitle());
            requestBody.put("bDesc", build.getBuildDescription());
            requestBody.put("bActive", build.getIsBuildActive());
            requestBody.put("bOpen", build.getIsBuildOpen());
            requestBody.put("bReleaseDate", build.getBuildReleaseDate());
            requestBody.put("bVersion", build.getBuildVersion());

            String updateUrl = HYPERLEDGER_BASE_URL + "/UpdateBuild";
            String log = restTemplate.postForObject(updateUrl, requestBody, String.class);
            logger.info("Build created successfully: {}", log);
            redirectAttributes.addFlashAttribute("success", "Build updated successfully");

        } catch (RestClientException e) {
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException httpException = (HttpClientErrorException) e;
                if (httpException.getStatusCode() == HttpStatus.NOT_FOUND) {
                    redirectAttributes.addFlashAttribute("error", "Build not found.");
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
        return "redirect:/viewBuilds";

    }

    public List<Build> viewBuilds(String filter, String search) {
        String url = HYPERLEDGER_BASE_URL + "/getAllBuilds";

        try {
            // Call the Node.js API to get all builds
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

            // Parse the response JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            if (!rootNode.has("message")) {
                throw new RuntimeException("Invalid API response format: 'message' key is missing.");
            }

            // Extract 'message' and map it to Build[]
            Build[] buildArray = objectMapper.convertValue(rootNode.get("message"), Build[].class);
            List<Build> allBuilds = Arrays.asList(buildArray);

            // Apply filtering and searching
            List<Build> filteredBuilds = applyFilter(allBuilds, filter);
            return applySearch(filteredBuilds, search);

        } catch (RestClientException e) {
            throw new RuntimeException("Error calling Node.js API: " + e.getMessage(), e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON response: " + e.getMessage(), e);
        }
    }

    private List<Build> applyFilter(List<Build> builds, String filter) {
        if (builds == null) {
            return Collections.emptyList();
        }

        // Return all builds if filter is null, empty, or "all"
        if (filter == null || filter.isEmpty() || "all".equalsIgnoreCase(filter)) {
            return builds;
        }

        // Filter for active builds only
        if ("active".equalsIgnoreCase(filter)) {
            return builds.stream()
                    .filter(build -> "true".equalsIgnoreCase(build.getIsBuildActive()))
                    .collect(Collectors.toList());
        }

        // Filter for inactive builds only
        else if ("inactive".equalsIgnoreCase(filter)) {
            return builds.stream()
                    .filter(build -> "false".equalsIgnoreCase(build.getIsBuildActive()))
                    .collect(Collectors.toList());
        } else {

            return builds; // Return all builds if no valid filter is applied
        }
    }

    private List<Build> applySearch(List<Build> builds, String search) {
        if (builds == null) {
            return Collections.emptyList();
        }

        if (search == null || search.trim().isEmpty()) {
            return builds;
        }

        String searchTerm = search.toLowerCase().trim();

        return builds.stream()
                .filter(build -> (build.getBuildTitle() != null &&
                        build.getBuildTitle().toLowerCase().contains(searchTerm)) ||
                        (build.getBuildDescription() != null &&
                                build.getBuildDescription().toLowerCase().contains(searchTerm))
                        ||
                        (build.getBuildVersion() != null &&
                                build.getBuildVersion().toLowerCase().contains(searchTerm)))
                .collect(Collectors.toList());
    }

}
