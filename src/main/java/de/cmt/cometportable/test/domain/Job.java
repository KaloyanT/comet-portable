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
        EXPORTED        // job is exported. waiting for the job results
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

    private List<JobResult> results;

    private JobType type;

    private String title;

    private String environment_address;

    private EnvironmentType environment_type = EnvironmentType.LOCAL;

    private List<Environment> environments;

    private boolean linkedEnvironment = true;

    private boolean localEnvironment = false;

    private boolean importTestResultsOnJobCompletion = true;

    private boolean hasMapping;

    public boolean isLinkedEnvironment() {
        // return linkedEnvironment;
        return this.getEnvironmentAddress() == null;
    }

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

    public List<JobResult> getResults() {
        return results;
    }

    public void setResults(List<JobResult> results) {
        this.results = results;
    }

    public void addResultItem(JobResult result) {
        this.results.add(result);
        result.setJob(this);
    }

    public void removeResultItem(JobResult result) {
        this.results.remove(result);
        result.setJob(null);
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

    public boolean getImportTestResultsOnJobCompletion() {
        return importTestResultsOnJobCompletion;
    }

    public void setImportTestResultsOnJobCompletion(boolean importTestResultsOnJobCompletion) {
        this.importTestResultsOnJobCompletion = importTestResultsOnJobCompletion;
    }

    public boolean isLocalEnvironment() {
        return localEnvironment;
    }

    public void setLocalEnvironment(boolean localEnvironment) {
        this.localEnvironment = localEnvironment;
    }

    public boolean getHasMapping() {
        return hasMapping;
    }

    public void setHasMapping(boolean hasMapping) {
        this.hasMapping = hasMapping;
    }
}
