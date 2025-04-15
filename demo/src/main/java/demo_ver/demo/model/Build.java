package demo_ver.demo.model;

public class Build {
    private String buildID; // Unique Build ID
    private String buildTitle; // Title of the Build
    private String buildDescription; // Description of the Build
    private String isBuildActive; // Status: Active or Inactive
    private String isBuildOpen; // Status: Open or Closed
    private String buildReleaseDate; // Release Date
    private String buildVersion; // Version of the Build

    public Build(String buildID, String buildTitle, String buildDescription,
            String isBuildActive, String isBuildOpen, String buildReleaseDate, String buildVersion) {
        this.buildID = buildID;
        this.buildTitle = buildTitle;
        this.buildDescription = buildDescription;
        this.isBuildActive = isBuildActive;
        this.isBuildOpen = isBuildOpen;
        this.buildReleaseDate = buildReleaseDate;
        this.buildVersion = buildVersion;
    }

    public Build() {

    }

    public Build(String buildID) {
        this.buildID = buildID;
        // Optionally, set default values for other fields, or fetch from a database if
        // needed
    }

    // Getters and Setters
    public String getBuildID() {
        return buildID;
    }

    public void setBuildID(String buildID) {
        this.buildID = buildID;
    }

    public String getBuildTitle() {
        return buildTitle;
    }

    public void setBuildTitle(String buildTitle) {
        this.buildTitle = buildTitle;
    }

    public String getBuildDescription() {
        return buildDescription;
    }

    public void setBuildDescription(String buildDescription) {
        this.buildDescription = buildDescription;
    }

    public String getIsBuildActive() {
        return isBuildActive;
    }

    public void setIsBuildActive(String isBuildActive) {
        this.isBuildActive = isBuildActive;
    }

    public String getIsBuildOpen() {
        return isBuildOpen;
    }

    public void setIsBuildOpen(String isBuildOpen) {
        this.isBuildOpen = isBuildOpen;
    }

    public String getBuildReleaseDate() {
        return buildReleaseDate;
    }

    public void setBuildReleaseDate(String buildReleaseDate) {
        this.buildReleaseDate = buildReleaseDate;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    public Object getStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStatus'");
    }

    public boolean isActive() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isActive'");
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }
}