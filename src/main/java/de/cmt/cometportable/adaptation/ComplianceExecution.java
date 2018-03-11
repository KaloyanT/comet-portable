package de.cmt.cometportable.adaptation;

import de.cmt.cometportable.test.TestRunner;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.Job.JobState;
import de.cmt.cometportable.test.plugin.TestRunnerFactory;
import de.cmt.cometportable.util.CometService;
import de.cmt.cometportable.util.CometShellUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class ComplianceExecution extends ComplianceRun {

    @Autowired
    private CometShellUtil cometShellUtil;

    @Autowired
    private CometService cometService;

    public ComplianceExecution(Job job) {
        super(job);
    }

    @Override
    protected synchronized void setExecutionJob() {
        this.log.debug("Setting Up Execution Job Info - Job ID {} ", this.job.getId());
        this.job = this.setJob(job);

        if(this.job == null) {
            this.log.error("Job is not found - got null! ");
            // run again
            this.setExecutionJob();

        } else if(this.job.getArtifact() == null) {
            this.log.error("No artifact found on job - got null!");
        }
    }

    private File setExportFolder(Job job) {
        String jobFolder = job.getArtifactType() + ".job." + this.getJob().getId().toString();
        return new File("jobs/", jobFolder);
    }

    @Override
    public void run() {

        this.setExecutionJob();
        this.job.setState(JobState.RUNNING);

        File exportDestination = this.setExportFolder(this.job);
        TestRunner runner = TestRunnerFactory.getRunner();

        // No PlaceHolderResolver is needed here since the test files are already created
        runner.setExportDestination(exportDestination);
        runner.execute(this.job);

        // update job properties
        this.job.setResult(runner.getResult());
        this.job.setState(JobState.FINISHED);

        this.cometShellUtil.saveJobResults(this.job);

        if(this.job.getImportTestResultsOnJobCompletion()) {
            this.cometService.importJobResults(this.job);
        }

    }

}

