package de.cmt.cometportable.shell;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.adaptation.ComplianceExecution;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import de.cmt.cometportable.test.domain.JobResultItem;
import de.cmt.cometportable.util.CometService;
import de.cmt.cometportable.util.CometShellUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

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

    private final Executor taskExecutor;

    public CometShell(Executor executor) {
        this.taskExecutor = executor;
    }

    @ShellMethod("Runs the specified Job and returns the results in the specified way")
    public void run(@ShellOption(value = {"-j", "--job-id"}) Long jobId,
                    @ShellOption(value = {"-i", "--import-results"}, help = "Import the results to COMET") boolean importResults,
                    @ShellOption(value = {"-c", "--comet-instance"}, defaultValue = "0") Long cometInstance) {

        Job job = cometShellUtil.createJob(jobId);

        if(job == null) {
            return;
        }

        // If the user chose not to import the test results back to COMET
        // immediately after Job completion, only save them to a file
        if(importResults == false) {
            job.setImportTestResultsOnJobCompletion(false);
        }

        // create Execution based on Job
        ComplianceExecution execution = new ComplianceExecution(job);
        beanFactory.autowireBean(execution);

        this.taskExecutor.execute(execution);

        log.info("Starting Job {} asynchronously. You will be notified after every status update", job.getId());
        log.info("Return to Shell by pressing ENTER");

    }

    @ShellMethod("Lists all Jobs that are Exported by COMET")
    public String list(@ShellOption(value = {"-c", "--comet-instance"}, defaultValue = "0") Long cometInstance) {

        List<ObjectNode> exportedJobs = this.cometService.getExportedJobs();

        if(exportedJobs == null) {
            log.error("Cannot list exported Jobs. No connection to COMET");
            return null;
        }

        return cometShellUtil.listExportedJobs(exportedJobs);
    }

    @ShellMethod("Prints the results for a given Job and/or imports them to a COMET Instance")
    public String res(@ShellOption(value = {"-j", "--job-id"}) Long jobId,
                      @ShellOption(value = {"-p", "--print"}) boolean printResults,
                      @ShellOption(value = {"-m", "--message"}, help = "Prints the Executor Message") boolean printMesssage,
                      @ShellOption(value = {"-i", "--import-results"}) boolean importResults,
                      @ShellOption(value = {"-c", "--comet-instance"}, defaultValue =  "0") Long cometInstance) {

        JobResult jobResult = cometShellUtil.getJobResult(jobId);

        if(jobResult == null || jobResult.getItems().size() < 3) {
            return null;
        }

        StringBuilder res = new StringBuilder();

        if(printResults == true) {
            res.append("Connection Test: ");
            res.append(jobResult.getItems().get(0).getType());
            res.append("\n");

            res.append("InSpec Profile Check: ");
            res.append(jobResult.getItems().get(1).getType());
            res.append("\n");

            res.append("InSpec Test Result: ");
            res.append(jobResult.getItems().get(2).getType());
            res.append("\n");
        }

        if(printMesssage == true) {
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

        log.error("Job {} doesn't exist at COMET instance {}", jobId, cometInstance);
    }
}
