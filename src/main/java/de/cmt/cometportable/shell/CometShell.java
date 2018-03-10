package de.cmt.cometportable.shell;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.adaptation.ComplianceExecution;
import de.cmt.cometportable.test.domain.Job;
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
    public void run(@ShellOption(value = {"-r", "--result-type"}, defaultValue = "i") String resultType,
                    @ShellOption(value = {"-j", "--job-id"}) Long jobId,
                    @ShellOption(value = {"-c", "--comet-instance"}, defaultValue = "0") Long cometInstance) {

        Job job = cometShellUtil.createJob(jobId);

        if(job == null) {
            return;
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
                      @ShellOption(value = {"-a", "--action"}, defaultValue = "p") String action,
                      @ShellOption(value = {"-c", "--comet-instance"}, defaultValue =  "0") Long cometInstance) {

        String res = cometShellUtil.getJobResult(jobId);

        return res;
    }

    @ShellMethod("Downloads the test files for a given Job")
    public void download(@ShellOption(value = {"-j", "--job-id"}) Long jobId,
                         @ShellOption(value = {"-c", "--comet-instance"}, defaultValue =  "0") Long cometInstance) {

        log.error("Job {} doesn't exist at COMET instance {}", jobId, cometInstance);
    }
}
