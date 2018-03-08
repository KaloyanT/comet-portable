package de.cmt.cometportable.adaptation;

import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComplianceRun implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    // protected final long jobId;

    protected Job job;

    protected JobResult jobResult;

    public ComplianceRun(Job job) {
        this.job = job;
    }

    public final synchronized Job setJob(Job job) {
        // this.job = this.jobRepsitory.findOne(jobId);
        this.job = job;
        return this.job;
    }

    protected abstract void setExecutionJob();

    public Job getJob() {
        return job;
    }

}
