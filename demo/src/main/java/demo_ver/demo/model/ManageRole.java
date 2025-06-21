package demo_ver.demo.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// @EntityScan
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageRole {

    @JsonProperty("roleID")
    private int roleID;

    @JsonProperty("description")
    private String description;

    @JsonProperty("roleName")
    private String roleName;

    @JsonProperty("isActive")
    private String isActive; 

    public ManageRole() {

    }

    public ManageRole(String roleName, String description, String isActive) {
        this.roleName = roleName;
        this.description = description;
        this.isActive = isActive;
    }

    public ManageRole(int roleID, String roleName, String description, String isActive) {
        this.roleID = roleID;
        this.roleName = roleName;
        this.description = description;
        this.isActive = isActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }
    
    // For role based authorization
    public List<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(roleName));
        // Add additional authorities as needed
        return authorities;
    }

}
