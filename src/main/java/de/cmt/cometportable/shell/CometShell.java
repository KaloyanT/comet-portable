package de.cmt.cometportable.shell;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.adaptation.ComplianceExecution;
import de.cmt.cometportable.test.domain.Environment;
import de.cmt.cometportable.test.domain.EnvironmentAuthenticationType;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import de.cmt.cometportable.test.domain.JobStringConstants;
import de.cmt.cometportable.util.CometService;
import de.cmt.cometportable.util.CometShellUtil;
import de.cmt.cometportable.util.JobMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;


@ShellComponent
public class CometShell {

    private final Logger log = LoggerFactory.getLogger(CometShell.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @Autowired
    private CometService cometService;

    @Autowired
    private CometShellUtil cometShellUtil;

    @Autowired
    private JobMonitoringService jobMonitoringService;

    private final Executor taskExecutor;

    public CometShell(Executor executor) {
        this.taskExecutor = executor;
    }

    @ShellMethod("Runs the specified Job and returns the results in the specified way")
    public void run(@ShellOption(value = {"-j", "--job-id"}) Long jobId,
                    @ShellOption(value = {"-i", "--import-results"}, help = "Import the results to COMET") boolean importResults,
                    @ShellOption(value = {"-l", "--local-env"}, help = "Runs the tests on this system") boolean localEnvironment,
                    @ShellOption(value = {"-k", "--key-file"}, defaultValue = "",help = "SSH Key File") String sshKeyFile,
                    @ShellOption(value = {"-c", "--comet-instance"}, defaultValue = "0") Long cometInstance) {

        Job job = cometShellUtil.createJob(jobId);

        log.info(sshKeyFile);

        if(job == null) {
            return;
        }

        // If the user chose not to import the test results back to COMET
        // immediately after Job completion, only save them to a file
        if(importResults == false) {
            job.setImportTestResultsOnJobCompletion(false);
        }

        if(localEnvironment == true) {
            job.setLocalEnvironment(true);

        // Handle the other environments too
        } else if(job.getEnvironments().get(0).getAuthenticationType() == EnvironmentAuthenticationType.KEY && sshKeyFile != null && !sshKeyFile.isEmpty()) {
            List<Environment> environments = job.getEnvironments();
            Environment env = environments.get(0);
            env.setKeyFile(sshKeyFile);
            environments.set(0, env);
            job.setEnvironments(environments);
        }

        // create Execution based on Job
        ComplianceExecution execution = new ComplianceExecution(job);
        beanFactory.autowireBean(execution);

        this.taskExecutor.execute(execution);

        log.info("Starting Job {} asynchronously. You will be notified after every status update", job.getId());
        log.info("Return to Shell by pressing ENTER");

    }

    @ShellMethod("Lists all Jobs that are Exported by COMET")
    public String list(@ShellOption(value = {"-d", "--downloaded-jobs"}) boolean downloadedJobs,
                       @ShellOption(value = {"-r", "--running-jobs"}) boolean runningJobs,
                       @ShellOption(value = {"-f", "--finished-jobs"}) boolean finishedJobs,
                       @ShellOption(value = {"-c", "--comet-instance"}, defaultValue = "0") Long cometInstance) {

        String res = null;

        if(downloadedJobs == true) {

            res = this.cometShellUtil.listJobsFromJobMonitoringService(this.jobMonitoringService.getDownloadedJobs());

        } else if(runningJobs == true) {

            res = this.cometShellUtil.listJobsFromJobMonitoringService(this.jobMonitoringService.getRunningJobs());

        } else if(finishedJobs == true) {

            res = this.cometShellUtil.listJobsFromJobMonitoringService(this.jobMonitoringService.getFinishedJobs());

        } else {

            List<ObjectNode> exportedJobs = this.cometService.getExportedJobs();

            if(exportedJobs == null) {
                log.error("Cannot list exported Jobs. No connection to COMET");
                return null;
            }

            res = cometShellUtil.listExportedJobs(exportedJobs);
        }

        return res;

    }

    @ShellMethod("Prints the results for a given Job and/or imports them to a COMET Instance")
    public String res(@ShellOption(value = {"-j", "--job-id"}) Long jobId,
                      @ShellOption(value = {"-m", "--message"}, help = "Prints the Executor Message") boolean printMessage,
                      @ShellOption(value = {"-i", "--import-results"}) boolean importResults,
                      @ShellOption(value = {"-c", "--comet-instance"}, defaultValue =  "0") Long cometInstance) {

        JobResult jobResult = cometShellUtil.getJobResult(jobId);

        if(jobResult == null || jobResult.getItems().size() < 3) {
            return null;
        }

        StringBuilder res = new StringBuilder();

        res.append("Connection Test: ");
        res.append(jobResult.getItems().get(0).getType());
        res.append("\n");

        res.append("InSpec Profile Check: ");
        res.append(jobResult.getItems().get(1).getType());
        res.append("\n");

        res.append("InSpec Test Result: ");
        res.append(jobResult.getItems().get(2).getType());
        res.append("\n");


        if(printMessage == true) {
            res.append("InSpec Profile Check Executor Message: ");
            res.append(jobResult.getItems().get(1).getExecutor_message());
            res.append("\n");

            res.append("InSpec Test Result Executor Message: ");
            res.append(jobResult.getItems().get(2).getExecutor_message());
            res.append("\n");
        }

        if(importResults == true) {

            // Create the Job Object again by reading it's configuration and set its results
            Job job = cometShellUtil.createJob(jobId);
            job.setResult(jobResult);
            job.setState(Job.JobState.FINISHED);
            cometService.importJobResults(job);
        }

        return res.toString();
    }

    @ShellMethod("Downloads the test files for a given Job")
    public void download(@ShellOption(value = {"-j", "--job-id"}) Long jobId,
                         @ShellOption(value = {"-c", "--comet-instance"}, defaultValue =  "0") Long cometInstance) {

        File jobFile = cometService.downloadJobAsZIP(jobId);

        if(jobFile == null) {
            log.error("Job {} doesn't exist at COMET instance {} or no connection to COMET", jobId, cometInstance);
            return;
        }

        cometShellUtil.unzipJobFiles(jobId, jobFile);

        this.jobMonitoringService.addDownloadedJob(JobStringConstants.getJobsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + jobId);
    }

    @ShellMethod("Performs a manual authorization with a COMET Instance")
    public void authenticate(@ShellOption(value = {"-c", "--comet-instance"}, defaultValue =  "0") Long cometInstance) {
        cometService.authenticate();
    }
}
