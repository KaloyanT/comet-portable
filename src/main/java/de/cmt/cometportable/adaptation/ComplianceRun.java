package de.cmt.cometportable.adaptation;

import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComplianceRun implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected final long jobId;

    protected Job job;

    protected JobResult jobResult;

    public ComplianceRun(long jobId) {
        this.jobId = jobId;
    }

    public final synchronized Job setJob(long jobId) {
        // this.job = this.jobRepsitory.findOne(jobId);
        return this.job;
    }

    protected abstract void setExecutionJob();

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

}
