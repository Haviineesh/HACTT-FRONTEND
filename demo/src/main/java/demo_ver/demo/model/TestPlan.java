package demo_ver.demo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPlan {
    private String testPlanID;
    private String testPlanName;
    private String description;
    private String isActive; // Changed from Boolean to String
    private String isPublic; // Changed from Boolean to String
    private String createdBy;
    private String dateCreated;
    private String updatedBy;
    private String dateUpdated;
    private TestSuite testSuite;
    private List<TestSuite> assignedTestSuites = new ArrayList<>();
    private Build assignedBuildID;

    public TestPlan(String testPlanID) {
        this.testPlanID = testPlanID;
        // Optionally, set default values for other fields, or fetch from a database if
        // needed
    }

    public TestPlan() {
    }

    // Constructor
    public TestPlan(String testPlanID, String testPlanName, String description, String isActive, String isPublic,
            String createdBy, String dateCreated) {
        this.testPlanID = testPlanID;
        this.testPlanName = testPlanName;
        this.description = description;
        this.isActive = isActive;
        this.isPublic = isPublic;
        this.createdBy = createdBy;
        this.dateCreated = dateCreated;
    }

    // Getters and Setters
    public String getTestPlanID() {
        return testPlanID;
    }

    public void setTestPlanID(String testPlanID) {
        this.testPlanID = testPlanID;
    }

    public String getTestPlanName() {
        return testPlanName;
    }

    public void setTestPlanName(String testPlanName) {
        this.testPlanName = testPlanName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    // Getter and Setter for assignedTestPlans
    public List<TestSuite> getAssignedTestSuite() {
        return assignedTestSuites;
    }

    public void setAssignedTestSuites(List<TestSuite> assignedTestSuites) {
        this.assignedTestSuites = assignedTestSuites;
    }

    public Build getAssignedBuildID() {
        return assignedBuildID;
    }

    public void setAssignedBuild(Build assignedBuildID) {
        this.assignedBuildID = assignedBuildID;
    }
}
