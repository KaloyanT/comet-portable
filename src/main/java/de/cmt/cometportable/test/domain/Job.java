package de.cmt.cometportable.test.domain;

import java.io.Serializable;

public class Job implements Serializable {

    private static final long serialVersionUID = -9077302891485992943L;

    public enum JobState {
        NEW,			// created and not yet queued
        QUEUED,			// queued in thread pool
        RUNNING,		// currently running
        FINISHED		// job finished
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

    private Serializable artifact;

    private JobState state;

    private JobResult result;

    private JobType type;

    private String enviroment_address;

    private EnvironmentType enviroment_type = EnvironmentType.LOCAL;

    private boolean linkedEnvironment = true;

    public boolean isLinkedEnvironment() {
        // return linkedEnvironment;
        return this.getEnviromentAddress() == null;
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

    public Serializable getArtifact() {
        return artifact;
    }

    public void setArtifact(Serializable artifact) {
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

    public String getEnviromentAddress() {
        return enviroment_address;
    }

    public void setEnviromentAddress(String enviroment_address) {
        this.enviroment_address = enviroment_address;
        this.linkedEnvironment = false;
    }

    public EnvironmentType getEnviromentType() {
        return enviroment_type;
    }

    public void setEnviromentType(EnvironmentType enviroment_type) {
        this.enviroment_type = enviroment_type;
    }

}
