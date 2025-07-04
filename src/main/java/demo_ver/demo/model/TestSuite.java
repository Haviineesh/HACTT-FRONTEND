package demo_ver.demo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSuite {
    private String testSuiteID;
    private String testSuiteName;
    private String testSuiteDesc;
    private String testSuiteStatus; // Overall status of the suite (e.g., Active, Inactive)
    private String importance; // Importance level (e.g., High, Medium, Low)
    private String createdBy;
    private String dateCreated;
    private List<Integer> assignedUserIds = new ArrayList<>(); // List of assigned user IDs
    private Map<Integer, String> userStatuses = new HashMap<>(); // Map of user IDs to their statuses (e.g., Pending,
                                                                 // Completed)
    private TestPlan testPlan;

    public TestSuite(String testSuiteID) {
        this.testSuiteID = testSuiteID;
        // Optionally, set default values for other fields, or fetch from a database if
        // needed
    }

    public TestSuite() {
    }

    // Constructor
    public TestSuite(String testSuiteID, String testSuiteName, String testSuiteDesc, String createdBy,
            String dateCreated) {
        this.testSuiteID = testSuiteID;
        this.testSuiteName = testSuiteName;
        this.testSuiteDesc = testSuiteDesc;
        this.testSuiteStatus = "Pending"; // Default status when created
        this.importance = "Medium";
        this.createdBy = createdBy;
        this.dateCreated = dateCreated; // Default importance level
    }

    public String getTestSuiteID() {
        return testSuiteID;
    }

    public void setId(String testSuiteID) {
        this.testSuiteID = testSuiteID;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }

    public String getTestSuiteDesc() {
        return testSuiteDesc;
    }

    public void setTestSuiteDesc(String testSuiteDesc) {
        this.testSuiteDesc = testSuiteDesc;
    }

    public String getTestSuiteStatus() {
        return testSuiteStatus;
    }

    public void setStatus(String testSuiteStatus) {
        this.testSuiteStatus = testSuiteStatus;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<Integer> getAssignedUserIds() {
        return assignedUserIds;
    }

    public void setAssignedUserIds(List<Integer> assignedUserIds) {
        this.assignedUserIds = assignedUserIds;
    }

    public Map<Integer, String> getUserStatuses() {
        return userStatuses;
    }

    public void setUserStatuses(Map<Integer, String> userStatuses) {
        this.userStatuses = userStatuses;
    }

    // Assign a user to the test suite
    public void assignUser(Integer userId) {
        if (!assignedUserIds.contains(userId)) {
            assignedUserIds.add(userId);
            userStatuses.put(userId, "Pending"); // Default status for a newly assigned user
        }
    }

    // Unassign a user from the test suite
    public void unassignUser(Integer userId) {
        assignedUserIds.remove(userId);
        userStatuses.remove(userId); // Remove the user's status as well
    }

    // Update the status of a specific user
    public void updateUserStatus(Integer userId, String status) {
        if (userStatuses.containsKey(userId)) {
            userStatuses.put(userId, status);
        } else {
            throw new IllegalArgumentException("User ID not assigned to the test suite");
        }
    }

    // Check if all users have a specific status (e.g., Completed)
    public boolean areAllUsersStatus(String status) {
        return userStatuses.values().stream().allMatch(s -> s.equals(status));
    }

    // Update overall status based on user statuses
    public void updatestatus() {
        if (areAllUsersStatus("Completed")) {
            testSuiteStatus = "Completed";
        } else if (areAllUsersStatus("In Progress")) {
            testSuiteStatus = "In Progress";
        } else {
            testSuiteStatus = "Pending";
        }
    }

    // Debugging utility
    @Override
    public String toString() {
        return "TestSuite{" +
                "id=" + testSuiteID +
                ", name='" + testSuiteName + '\'' +
                ", description='" + testSuiteDesc + '\'' +
                ", overallStatus='" + testSuiteStatus + '\'' +
                ", importance='" + importance + '\'' +
                ", assignedUserIds=" + assignedUserIds +
                ", userStatuses=" + userStatuses +
                '}';
    }

    public TestPlan getTestPlan() {
        return testPlan;
    }

    public void setTestPlan(TestPlan testPlan) {
        this.testPlan = testPlan;
    }

}
