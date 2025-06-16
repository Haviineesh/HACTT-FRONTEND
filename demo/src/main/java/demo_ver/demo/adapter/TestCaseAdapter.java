package demo_ver.demo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import demo_ver.demo.model.TestCase;

@Component
public class TestCaseAdapter {

    public List<TestCase> convertJsonToTestCases(JsonNode root) {
        List<TestCase> testCases = new ArrayList<>();

        for (JsonNode node : root) {
            TestCase testCase = convertJsonToTestCase(node);
            if (testCase != null) {
                testCases.add(testCase);
            }
        }

        return testCases;
    }

    public TestCase convertJsonToTestCase(JsonNode node) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TestCase testCase = new TestCase();

            testCase.setIdtest_cases(Long.parseLong(node.get("idtest_cases").asText()));
            testCase.setTest_desc(node.get("test_desc").asText());
            testCase.setDeadline(node.get("deadline").asText());
            testCase.setDateUpdated(node.get("dateUpdated").asText());
            testCase.setProjectId(node.get("projectId").asText());
            testCase.setReason(node.get("reason").asText());
            testCase.setTestCaseName(node.get("testCaseName").asText());
            testCase.setDateCreated(node.get("dateCreated").asText());
            testCase.setOverallStatus(node.get("overallStatus").asText());
            testCase.setUsername(node.get("username").asText());
            testCase.setCreatedBy(node.get("createdBy").asText());
            testCase.setStatus(node.get("status").asText());

            ObjectMapper stringMapper = new ObjectMapper();

            String userIdRaw = sanitize(node.get("userID").asText());
            testCase.setUserID(stringMapper.readValue(userIdRaw, new TypeReference<List<Integer>>() {}));

            String stepsRaw = sanitize(node.get("tcSteps").asText());
            testCase.setTcSteps(stringMapper.readValue(stepsRaw, new TypeReference<List<String>>() {}));

            String resultsRaw = sanitize(node.get("expectedResults").asText());
            testCase.setExpectedResults(stringMapper.readValue(resultsRaw, new TypeReference<List<String>>() {}));

            String statusesRaw = sanitize(node.get("userStatuses").asText());
            testCase.setUserStatuses(stringMapper.readValue(statusesRaw, new TypeReference<Map<String, String>>() {}));

            String reasonsRaw = sanitize(node.get("userReasons").asText());
            testCase.setUserReason(stringMapper.readValue(reasonsRaw, new TypeReference<Map<String, String>>() {}));

            return testCase;
        } catch (Exception e) {
            System.err.println("Failed to parse TestCase JSON: " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> convertTestCaseToJsonStringifiedFormat(TestCase testCase) {
        Map<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            map.put("idtest_cases", String.valueOf(testCase.getIdtest_cases()));
            map.put("test_desc", safeString(testCase.getTest_desc()));
            map.put("deadline", safeString(testCase.getDeadline()));
            map.put("dateUpdated", safeString(testCase.getDateUpdated()));
            map.put("projectId", safeString(testCase.getProjectId()));
            map.put("reason", safeString(testCase.getReason()));
            map.put("testCaseName", safeString(testCase.getTestCaseName()));
            map.put("dateCreated", safeString(testCase.getDateCreated()));
            map.put("userID", mapper.writeValueAsString(
                    testCase.getUserID() != null ? testCase.getUserID() : new ArrayList<Integer>()));
            map.put("userStatuses", mapper.writeValueAsString(
                    testCase.getUserStatuses() != null ? testCase.getUserStatuses() : new HashMap<String, String>()));
            map.put("overallStatus", safeString(testCase.getOverallStatus()));
            map.put("username", safeString(testCase.getUsername()));
            map.put("createdBy", safeString(testCase.getCreatedBy()));
            map.put("status", safeString(testCase.getStatus()));
            map.put("userReasons", mapper.writeValueAsString(
                    testCase.getUserReason() != null ? testCase.getUserReason() : new HashMap<String, String>()));
            map.put("tcSteps", mapper.writeValueAsString(
                    testCase.getTcSteps() != null ? testCase.getTcSteps() : new ArrayList<String>()));
            map.put("expectedResults", mapper.writeValueAsString(
                    testCase.getExpectedResults() != null ? testCase.getExpectedResults() : new ArrayList<String>()));
        } catch (Exception e) {
            System.err.println("Error serializing TestCase to blockchain format: " + e.getMessage());
        }

        return map;
    }

    private String safeString(String input) {
        return input != null ? input : "";
    }

    private static String sanitize(String jsonText) {
        if (jsonText == null) return "";
        return jsonText.replaceAll("^\\\"|\\\"$", "").replace("\\\"", "\"");
    }
}
