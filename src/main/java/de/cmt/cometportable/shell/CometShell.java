package de.cmt.cometportable.shell;

import de.cmt.cometportable.adaptation.ComplianceExecution;
import de.cmt.cometportable.test.domain.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.concurrent.Executor;


@ShellComponent
public class CometShell {

    private final Logger log = LoggerFactory.getLogger(CometShell.class);

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    private final Executor taskExecutor;

    public CometShell(Executor executor) {
        this.taskExecutor = executor;
    }

    @ShellMethod("Add two integers")
    public int add(int a, int b) {
        return a + b;
    }

    @ShellMethod("Executes some command")
    public void run(String parameter, String value) {

        // Figure out all the details for the Job object and create it properly
        Job job = new Job();


        // create Execution based on Job
        ComplianceExecution execution = new ComplianceExecution(job.getId());
        beanFactory.autowireBean(execution);

        log.debug("REST request to create execution job DONE");

        this.taskExecutor.execute(execution);
    }
}
