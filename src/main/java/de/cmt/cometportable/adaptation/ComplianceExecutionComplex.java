package de.cmt.cometportable.adaptation;

import de.cmt.cometportable.test.TestRunner;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.Job.JobState;
import de.cmt.cometportable.test.domain.JobResult;
import de.cmt.cometportable.test.plugin.TestRunnerFactory;
import de.cmt.cometportable.util.CometService;
import de.cmt.cometportable.util.CometShellUtil;
import de.cmt.cometportable.util.JobMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ComplianceExecutionComplex extends ComplianceRun {

    @Autowired
    private CometShellUtil cometShellUtil;

    @Autowired
    private CometService cometService;

    @Autowired
    private JobMonitoringService jobMonitoringService;

    public ComplianceExecutionComplex(Job job) {
        super(job);
    }

    @Override
    protected synchronized void setExecutionJob() {
        this.log.debug("Setting Up Complex Execution Job Info - Job ID {} ", this.job.getId());
        this.job = this.setJob(job);

        if(this.job == null) {
            this.log.error("Job is not found - got null! ");
            // run again
            this.setExecutionJob();

        } else if(this.job.getArtifact() == null) {
            this.log.error("No artifact found on job - got null!");
        }
    }

    private File setRootExportFolder(Job job) {
        String jobFolder = job.getArtifactType() + ".job." + this.getJob().getId().toString();
        return new File("jobs/", jobFolder);
    }

    private File setJobSpecificExportFolder(Job job, File rootFolder) {
        String jobFolder = job.getArtifactType() + ".job." + job.getId().toString();
        String rootFolderPath = rootFolder.getPath() + "/";
        return new File(rootFolderPath, jobFolder);
    }

    @Override
    public void run() {

        this.setExecutionJob();
        this.job.setState(JobState.RUNNING);

        File rootExportDestination = this.setRootExportFolder(this.job);

        if(this.jobMonitoringService.getRunningJobs().contains(rootExportDestination.toString())){
            log.error("Job {} is already running!", this.job.getId());
            return;

        } else if(this.jobMonitoringService.getFinishedJobs().contains(rootExportDestination.toString())) {
            log.error("Job {} has already finished!", this.job.getId());
            return;
        }

        this.jobMonitoringService.addRunningJob(rootExportDestination.toString());

        List<Job> subJobs = this.cometShellUtil.createSubJobs(this.job);
        List<JobResult> jobResultList = new ArrayList<>();
        TestRunner runner = TestRunnerFactory.getRunner();

        long currentJobId = 1;

        for(Job currentJob : subJobs) {

            currentJob.setId(currentJobId++);
            File jobFolder = this.setJobSpecificExportFolder(currentJob, rootExportDestination);

            currentJob.setState(JobState.RUNNING);

            runner.setExportDestination(jobFolder);
            runner.execute(currentJob);

            jobResultList.addAll(runner.getResults());

            currentJob.setState(JobState.FINISHED);
        }

        for(JobResult jr : jobResultList) {
            jr.setJob(this.job);
        }

        this.job.setResults(jobResultList);

        this.job.setState(JobState.FINISHED);

        this.jobMonitoringService.removeRunningJob(rootExportDestination.toString());
        this.jobMonitoringService.addFinishedJob(rootExportDestination.toString());

        this.cometShellUtil.saveJobResults(this.job);

        if(this.job.getImportTestResultsOnJobCompletion()) {
            this.cometService.importJobResults(this.job);
        }
    }
}
