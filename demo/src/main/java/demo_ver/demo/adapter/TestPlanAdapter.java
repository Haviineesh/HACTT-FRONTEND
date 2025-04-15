package demo_ver.demo.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import demo_ver.demo.model.TestPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class TestPlanAdapter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a JsonNode containing test plan data into a List of TestPlan
     * objects.
     *
     * @param rootNode the root JsonNode of the API response.
     * @return a List of TestPlan objects.
     */
    public List<TestPlan> convertJsonToTestPlans(JsonNode rootNode) {
        List<TestPlan> testPlans = new ArrayList<>();

        if (rootNode.has("message")) {
            JsonNode messageNode = rootNode.get("message");
            if (messageNode.isArray()) {
                for (JsonNode testPlanNode : messageNode) {
                    TestPlan testPlan = parseTestPlan(testPlanNode);
                    testPlans.add(testPlan);
                }
            }
        }

        return testPlans;
    }

    /**
     * Parses a single JsonNode into a TestPlan object.
     *
     * @param testPlanNode the JsonNode representing a single test plan.
     * @return a TestPlan object.
     */
    private TestPlan parseTestPlan(JsonNode testPlanNode) {
        TestPlan testPlan = new TestPlan();

        testPlan.setTestPlanID(testPlanNode.path("testPlanID").asText());
        testPlan.setTestPlanName(testPlanNode.path("testPlanName").asText());
        testPlan.setDescription(testPlanNode.path("description").asText());
        testPlan.setIsActive(testPlanNode.path("isActive").asText());
        testPlan.setIsPublic(testPlanNode.path("isPublic").asText());
        testPlan.setCreatedBy(testPlanNode.path("createdBy").asText());
        testPlan.setDateCreated(testPlanNode.path("dateCreated").asText());
        testPlan.setUpdatedBy(testPlanNode.path("updatedBy").asText());
        testPlan.setDateUpdated(testPlanNode.path("dateUpdated").asText());

        // Handle assigned test suites
        if (testPlanNode.has("assignedTestSuites") && testPlanNode.get("assignedTestSuites").isArray()) {
            List<String> assignedTestSuites = new ArrayList<>();
            Iterator<JsonNode> elements = testPlanNode.get("assignedTestSuites").elements();
            while (elements.hasNext()) {
                assignedTestSuites.add(elements.next().asText());
            }
            // Assuming TestSuite is another class; update if necessary
            // For now, store IDs as strings
        }

        // Handle assigned build
        if (testPlanNode.has("assignedBuild")) {
            String assignedBuild = testPlanNode.get("assignedBuild").asText();
            // Assuming Build is another class; update as needed
        }

        return testPlan;
    }

    /**
     * Converts a TestPlan object and assigned test suite IDs into a format suitable
     * for the backend.
     *
     * @param testPlan             The TestPlan object.
     * @param assignedTestSuiteIDs A list of assigned test suite IDs.
     * @return A map representing the request body.
     */
    public Map<String, Object> convertTestPlanToRequestBody(TestPlan testPlan, List<String> assignedTestSuiteIDs) {
        Map<String, Object> requestBody = new HashMap<>();

        // Add TestPlan fields
        requestBody.put("tpID", testPlan.getTestPlanID());
        requestBody.put("tpName", testPlan.getTestPlanName());
        requestBody.put("tpDesc", testPlan.getDescription());
        requestBody.put("createdBy", testPlan.getCreatedBy());
        requestBody.put("dateCreated", testPlan.getDateCreated());
        requestBody.put("isActive", testPlan.getIsActive());
        requestBody.put("isPublic", testPlan.getIsPublic());
        requestBody.put("updatedBy", testPlan.getUpdatedBy());
        requestBody.put("dateUpdated", testPlan.getDateUpdated());

        // Add assigned test suite IDs
        if (assignedTestSuiteIDs != null && !assignedTestSuiteIDs.isEmpty()) {
            requestBody.put("assignedTestSuiteIDs", assignedTestSuiteIDs);
        } else {
            requestBody.put("assignedTestSuiteIDs", new ArrayList<>());
        }

        return requestBody;
    }
}
