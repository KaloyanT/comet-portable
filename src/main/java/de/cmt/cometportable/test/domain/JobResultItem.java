package de.cmt.cometportable.test.domain;

import java.io.Serializable;

public class JobResultItem implements Serializable {

    private static final long serialVersionUID = -1036376033889026205L;

    private Long id;

    private ResultType type;

    private String executor;

    private String executor_message;

    private JobResult jobResult;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ResultType getType() {
        return type;
    }

    public void setType(ResultType type) {
        this.type = type;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getExecutor_message() {
        return executor_message;
    }

    public void setExecutor_message(String executor_message) {
        this.executor_message = executor_message;
    }

    public JobResult getJobResult() {
        return jobResult;
    }

    public void setJobResult(JobResult jobResult) {
        this.jobResult = jobResult;
    }

}

