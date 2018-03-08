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

    private final Executor taskExecutor;

    @Autowired
    private CometService cometService;

    @Autowired
    private CometShellUtil cometShellUtil;

    public CometShell(Executor executor) {
        this.taskExecutor = executor;
    }

    @ShellMethod("Add two integers")
    public int add(int a, int b) {
        return a + b;
    }

    @ShellMethod("Runs the specified Job and returns the results in the specified way")
    public void run(@ShellOption(value = {"-r", "--result-type"}, defaultValue = "i") String resultType,
                    @ShellOption(value = {"-j", "--job-id"}) Long jobId,
                    @ShellOption(value = {"-ci", "--comet-instance"}, defaultValue = "0") Long cometInstance) {

        // Figure out all the details for the Job object and create it properly
        // Read everything as a ObjectNode and then create the Job object
        Job job = new Job();
        job.setId(3512L);
        job.setArtifactType("de.cmt.domain.entity.artifact.CustomerProject");
        job.setState(Job.JobState.EXPORTED);
        job.setType(Job.JobType.EXECUTION);

        job.setTitle("Linux File Existence CP");
        job.setEnvironmentType(Job.EnvironmentType.LOCAL);
        job.setEnvironmentAddress(null);
        job.setArtifact("CustomerProject{id=234, title='Linux File Existence CP', " +
                "description='CP for the Linux File Existence Solution', artifactId='com.linux.file.example', " +
                "significance='2', version='1.0'}");

        // create Execution based on Job
        ComplianceExecution execution = new ComplianceExecution(job);
        beanFactory.autowireBean(execution);

        log.debug("REST request to create execution job DONE");

        this.taskExecutor.execute(execution);
    }

    @ShellMethod("Lists all Jobs that are Exported by COMET")
    public String list(@ShellOption(value = {"-ci", "--comet-instance"}, defaultValue = "0") Long cometInstance) {
        List<ObjectNode> exportedJobs = this.cometService.getExportedJobs();
        return cometShellUtil.listExportedJobs(exportedJobs);
    }
}
