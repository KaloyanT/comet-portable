package de.cmt.cometportable.test.plugin.inspec;

import de.cmt.cometportable.test.TestRunner;
import de.cmt.cometportable.test.domain.Environment;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import de.cmt.cometportable.test.domain.JobResultItem;
import de.cmt.cometportable.test.domain.ResultType;
import de.cmt.cometportable.test.plugin.inspec.evaluation.InspecEvaluate;
import de.cmt.cometportable.test.plugin.inspec.execution.InspecCommand;
import de.cmt.cometportable.test.plugin.inspec.execution.InspecOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InspecTestRunner implements TestRunner {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Required configuration for test flow
     */
    private Job job;

    private File exportDestination;

    private JobResult result;

    @Override
    public JobResult getResult() {
        return this.result;
    }

    @Override
    public TestRunner setExportDestination(File destination) {
        this.exportDestination = destination;
        return this;
    }

    @Override
    public JobResult execute(Job job) {

        this.job = job;
        this.result = new JobResult();

        // check correct state of class
        if(this.exportDestination == null) {
            throw new IllegalStateException(
                    "Export Destination is missing, stopping execution");
        }

        this.generateTestCode();
        this.executeTests();

        return this.result;
    }

    private void generateTestCode() {

    }

    private void executeTests() {
        this.connectionTest();
        this.inspecCheckProfile();
        this.inspecTestProfile();
    }

    private void inspecCheckProfile() {

        InspecCommand inspec = this.getInspecCommandRunner();
        InspecOutput inspecResult = inspec.check(exportDestination);
        InspecEvaluate inspecJson = new InspecEvaluate(String.join(" ", inspecResult.getLines()));

        this.addJobResultItem(inspec.getCommandString(), inspecResult.getLines(), inspecJson.getResult());
    }

    private void inspecTestProfile() {

        InspecCommand inspec = this.getInspecCommandRunner();
        InspecOutput inspecResult;

        // TODO not only take first ...
        Environment env = this.job.getEnvironments().get(0);

        inspecResult = inspec.test(this.exportDestination, env);

        InspecEvaluate inspecJson = new InspecEvaluate(String.join(" ", inspecResult.getLines()));

        this.log.info("Evaluated InSpec Exec - Result {}", inspecJson.getResult().toString());
        this.addJobResultItem(inspec.getCommandString(), inspecResult.getLines(), inspecJson.getResult());

    }

    private void connectionTest() {

        Environment env = new Environment();
        boolean reachable = true; // default if we have local:// or docker://

        /*
        if(this.job.isLinkedEnvironment()) {
            // TODO: handle this correctly or build back the possibility that
            // one can run artifacts in general
            CustomerProject pro = (CustomerProject) this.job.getArtifact();
            if(pro.hasEnvironments()) {
                for(Environment lEnv : pro.getEnvironments()) {
                    SSHConnection ssh = new SSHConnection(lEnv);
                    reachable &= ssh.hasValidAuthentication();
                }
            }

        } else if(this.job.getEnvironmentType() == EnvironmentType.SSH) {
            env.setHost(this.job.getEnvironmentAddress());
            SSHConnection ssh = new SSHConnection(env);
            reachable = ssh.isHostReachable();
        }
        */

        this.addJobResultItem(
                "Connectivity Test",
                new ArrayList<>(),
                (reachable) ? ResultType.VALID : ResultType.INVALID
        );
    }

    private void addJobResultItem(String executor, List<String> messages, ResultType type) {

        JobResultItem item = new JobResultItem();
        item.setJobResult(this.result);
        item.setExecutor(executor);
        item.setExecutor_message(String.join("\n\r", messages));
        item.setType(type);

        this.result.addItem(item);
    }

    private InspecCommand getInspecCommandRunner() {
        return new InspecCommand();
    }

}