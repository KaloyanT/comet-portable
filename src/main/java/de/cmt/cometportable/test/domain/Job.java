package de.cmt.cometportable.test.domain;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {

    private static final long serialVersionUID = -9077302891485992943L;

    public enum JobState {
        NEW,			// created and not yet queued
        QUEUED,			// queued in thread pool
        RUNNING,		// currently running
        FINISHED,		// job finished
        EXPORTED
    }

    public enum JobType {
        SIMULATION,
        EXECUTION
    }

    public enum EnvironmentType {
        LOCAL,
        SSH,
        DOCKER,
        WINRM
    }

    private Long id;

    private String artifactType;

    private String artifact;

    private JobState state;

    private JobResult result;

    private JobType type;

    private String environment_address;

    private EnvironmentType environment_type = EnvironmentType.LOCAL;

    private List<Environment> environments;

    private boolean linkedEnvironment = true;

    public boolean isLinkedEnvironment() {
        // return linkedEnvironment;
        return this.getEnvironmentAddress() == null;
    }

    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public JobResult getResult() {
        return result;
    }

    public void setResult(JobResult result) {
        this.result = result;
    }

    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public String getEnvironmentAddress() {
        return environment_address;
    }

    public void setEnvironmentAddress(String environment_address) {
        this.environment_address = environment_address;
        this.linkedEnvironment = false;
    }

    public EnvironmentType getEnvironmentType() {
        return environment_type;
    }

    public void setEnvironmentType(EnvironmentType environment_type) {
        this.environment_type = environment_type;
    }

    public List<Environment> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Environment> environments) {
        this.environments = environments;
    }
}
