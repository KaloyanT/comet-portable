package de.cmt.cometportable.test.domain;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

public class Environment implements Serializable {

    private static final long serialVersionUID = -4544140058447841104L;

    private Long id;

    private String title;

    private EnvironmentAuthenticationType authenticationType;

    private String host;

    private String user;

    private String password;

    private String keyFile;

    private String keyFingerprint;

    //private Set<CustomerProject> projects = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Environment title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EnvironmentAuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public Environment authenticationType(EnvironmentAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    public void setAuthenticationType(EnvironmentAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getHost() {
        return host;
    }

    public Environment host(String host) {
        this.host = host;
        return this;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public Environment user(String user) {
        this.user = user;
        return this;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public Environment password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getPublicKeyFile() {

        if(!StringUtils.isEmpty( this.keyFile ) ){
            // Original line from COMET IS:
            // return keyFile.concat(".").concat(Constants.PUBLIC_KEY_EXTENSION);
            // with Constants.PUBLIC_KEY_EXTENSION equal to "pub"
            return keyFile.concat(".").concat("pub");
        } else {
            return keyFile;
        }
    }

    public Environment keyFile(String keyFile) {
        this.keyFile = keyFile;
        return this;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getKeyFingerprint() {
        return keyFingerprint;
    }

    public Environment keyFingerprint(String keyFingerprint) {
        this.keyFingerprint = keyFingerprint;
        return this;
    }

    public void setKeyFingerprint(String keyFingerprint) {
        this.keyFingerprint = keyFingerprint;
    }

    /*
    public Set<CustomerProject> getProjects() {
        return projects;
    }

    public Environment projects(Set<CustomerProject> customerProjects) {
        this.projects = customerProjects;
        return this;
    }

    public Environment addProjects(CustomerProject customerProject) {
        this.projects.add(customerProject);
        customerProject.getEnvironments().add(this);
        return this;
    }

    public Environment removeProjects(CustomerProject customerProject) {
        this.projects.remove(customerProject);
        customerProject.getEnvironments().remove(this);
        return this;
    }

    public void setProjects(Set<CustomerProject> customerProjects) {
        this.projects = customerProjects;
    }
    */

    @Override
    public boolean equals(Object o) {

        if(this == o) {
            return true;
        }

        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        Environment environment = (Environment) o;

        if(environment.getId() == null || getId() == null) {
            return false;
        }

        return Objects.equals(getId(), environment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Environment{" +
                "id=" + getId() +
                ", title='" + getTitle() + "'" +
                ", authenticationType='" + getAuthenticationType() + "'" +
                ", host='" + getHost() + "'" +
                ", user='" + getUser() + "'" +
                ", keyFile='" + getKeyFile() + "'" +
                ", keyFingerprint='" + getKeyFingerprint() + "'" +
                "}";
    }

    public String getShorthandName() {
        return "ev";
    }
}

