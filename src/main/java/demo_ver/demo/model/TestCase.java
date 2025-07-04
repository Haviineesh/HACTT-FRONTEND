package demo_ver.demo.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.domain.EntityScan;

import demo_ver.demo.service.ManageUserService;

@EntityScan
public class TestCase {
    private Long idtest_cases;
    private String test_desc;
    private String deadline;
    private String dateUpdated;
    private String projectId;
    private String reason;
    private Map<String, String> userReasons = new HashMap<>();
    private String testCaseName;
    private String dateCreated;
    // private String smartContractID; // Changed from int to String
    // private List<Integer> userID;
    private List<Integer> userID;
    private Map<String, String> userStatuses = new HashMap<>(); // New field for user-specific statuses
    private String overallStatus;
    private String username;
    private String createdBy;
    private String status;
    private List<String> tcSteps;
    private List<String> expectedResults;
    private String testCaseVersion;

    public TestCase() {

    }

    public TestCase(String status, Long idtest_cases, String projectId, String testCaseName,
            String test_desc, String dateCreated, String deadline, String overallStatus, List<Integer> userID,
            String testCaseVersion) {
        this.status = status;
        this.idtest_cases = idtest_cases;
        this.projectId = projectId;
        this.testCaseName = testCaseName;
        this.test_desc = test_desc;
        this.dateCreated = dateCreated;
        this.deadline = deadline;
        this.overallStatus = overallStatus;
        this.userID = userID;
        this.testCaseVersion = testCaseVersion;

    }

    public void setUserStatuses(Map<String, String> userStatuses) {
        this.userStatuses = userStatuses;
    }

    // Getters and setters for existing fields
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getUserReason() {
        return userReasons;
    }

    public void setUserReason(Map<String, String> userReason) {
        this.userReasons = userReason;
    }

    public void setUserReason(String username, String reason) {
        userReasons.put(username, reason);
    }

    public Long getIdtest_cases() {
        return idtest_cases;
    }

    public void setIdtest_cases(Long idtest_cases) {
        this.idtest_cases = idtest_cases;
    }

    public String getTest_desc() {
        return test_desc;
    }

    public void setTest_desc(String test_desc) {
        this.test_desc = test_desc;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    // public String getSmartContractID() {
    // return smartContractID;
    // }

    // public void setSmartContractID(String smartContractID) {
    // this.smartContractID = smartContractID;
    // }

    public List<Integer> getUserID() {
        return userID;
    }

    public void setUserID(List<Integer> userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Methods for user statuses
    public Map<String, String> getUserStatuses() {
        return userStatuses;
    }

    public void setUserStatus(String username, String status) {
        userStatuses.put(username, status);
    }

    // Method to get usernames of assigned users
    public List<String> getUsernames(ManageUserService manageUserService) {
        return userID.stream()
                .map(userId -> {
                    ManageUser user = manageUserService.getUserById(userId);
                    return (user != null) ? user.getUsername() : "";
                })
                .collect(Collectors.toList());
    }

    public List<String> getTcSteps() {
        return tcSteps;
    }

    public void setTcSteps(List<String> tcSteps) {
        this.tcSteps = tcSteps;
    }

    public List<String> getExpectedResults() {
        return expectedResults;
    }

    public void setExpectedResults(List<String> expectedResults) {
        this.expectedResults = expectedResults;
    }

    public String getTestCaseVersion() {
        return testCaseVersion;
    }

    public void setTestCaseVersion(String testCaseVersion) {
        this.testCaseVersion = testCaseVersion;
    }

    // Method to determine overall status based on user statuses
    // METHOD 1
    // public String determineOverallStatus() {
    // if (userStatuses.containsValue("Rejected")) {
    // return "Rejected";
    // } else if (userStatuses.values().stream().allMatch(status ->
    // status.equals("Approved"))) {
    // return "Approved";
    // } else if (userStatuses.values().stream().anyMatch(status ->
    // status.equals("Under Review") || status.equals("Needs Revision"))) {
    // return "Pending";
    // } else {
    // return "Pending"; // Default to Pending if none of the above conditions are
    // met
    // }
    // }

    public void resetUserStatuses() {
        this.userStatuses.clear();
    }

    public String determineOverallStatus() {
        // If any user has rejected the test case, then the overall status is "Rejected"
        if (userStatuses.containsValue("Rejected")) {
            return "Rejected";
        }

        // Check if all assigned users have set their status
        // Only change the overall status if all users have set their status
        if (userStatuses.size() == userID.size()) {// minus the tester/ note tester must tick their own name
            // If all users have approved, then the overall status is "Approved"
            boolean allApproved = userStatuses.values().stream().allMatch(status -> status.equals("Approved"));
            if (allApproved) {
                return "Approved";
            }

            // incorrect method remove after db connection, tester doesn't need to be
            // included during creating test case after db connection
            // long nonApprovedCount = userStatuses.values().stream()
            // .filter(status -> !status.equals("Approved"))
            // .count();

            // long notSetCount = userStatuses.values().stream()
            // .filter(status -> status == null || status.isEmpty())
            // .count();

            // if (nonApprovedCount == 0 && notSetCount <= 1) {
            // return "Approved";
            // }

            // incorrect way end

            // If all users have "Needs Revision", then the overall status is "Needs
            // Revision"
            if (userStatuses.values().stream().allMatch(status -> status.equals("Needs Revision"))) {
                return "Needs Revision";
            }

            // If any user has set "Under Review" or "Needs Revision" without any "Reject",
            // then it's "Pending"
            boolean anyUnderReviewOrNeedsRevision = userStatuses.values().stream()
                    .anyMatch(status -> status.equals("Under Review") || status.equals("Needs Revision"));
            if (anyUnderReviewOrNeedsRevision) {
                return "Pending";
            }
        }

        // Default to "Pending" if not all users have set their status or if none of the
        // above conditions are met
        return "Pending";
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }
}