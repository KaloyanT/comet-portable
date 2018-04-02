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
import de.cmt.cometportable.util.SSHConnection;
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

    private List<JobResult> results;

    private int environmentCount;

    @Override
    public List<JobResult> getResults() {
        return this.results;
    }

    @Override
    public TestRunner setExportDestination(File destination) {
        this.exportDestination = destination;
        return this;
    }

    @Override
    public List<JobResult> execute(Job job) {

        this.job = job;
        this.results = new ArrayList<>();

        this.environmentCount = this.job.getEnvironments().size();

        for(int i = 0; i < environmentCount; i++) {
            JobResult temp = new JobResult();
            this.results.add(temp);
        }

        // check correct state of class
        if(this.exportDestination == null) {
            throw new IllegalStateException(
                    "Export Destination is missing, stopping execution");
        }

        this.generateTestCode();
        this.executeTests();

        return this.results;
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

        for(int i = 0; i < this.environmentCount; i++) {
            this.addJobResultItem(inspec.getCommandString(), inspecResult.getLines(), inspecJson.getResult(), i);
        }

    }

    private void inspecTestProfile() {

        InspecCommand inspec = this.getInspecCommandRunner();
        InspecOutput inspecResult;

        boolean multipleEnvironments = false;

        int i = 0;

        if(this.job.isLocalEnvironment()) {
            inspecResult = inspec.test(this.exportDestination);

        } else {

            multipleEnvironments = true;
            // There is always at least 1 Environment
            do {
                Environment env = this.job.getEnvironments().get(i);
                inspecResult = inspec.test(this.exportDestination, env);
                InspecEvaluate inspecJson = new InspecEvaluate(String.join(" ", inspecResult.getLines()));
                this.addJobResultItem(inspec.getCommandString(), inspecResult.getLines(), inspecJson.getResult(), i);
                i++;
            } while (i < this.environmentCount);
        }

        InspecEvaluate inspecJson = new InspecEvaluate(String.join(" ", inspecResult.getLines()));

        this.log.info("Evaluated InSpec Exec - Result {}", inspecJson.getResult().toString());

        if(multipleEnvironments == false) {
            this.addJobResultItem(inspec.getCommandString(), inspecResult.getLines(), inspecJson.getResult(), 0);
        }


    }

    private void connectionTest() {

        Environment env = new Environment();
        boolean reachable = true; // default if we have local:// or docker://

        if(this.job.isLinkedEnvironment() && this.job.isLocalEnvironment() == false) {
            // TODO: handle this correctly or build back the possibility that
            // one can run artifacts in general
            List<Environment> environments = this.job.getEnvironments();
            int index = 0;
            for(Environment lEnv : environments) {
                SSHConnection ssh = new SSHConnection(lEnv);
                reachable &= ssh.hasValidAuthentication();

                this.addJobResultItem(
                        "Connectivity Test",
                        new ArrayList<>(),
                        (reachable) ? ResultType.VALID : ResultType.INVALID,
                        index
                );

                index++;
            }

        } else if(this.job.getEnvironmentType() == Job.EnvironmentType.SSH) {
            env.setHost(this.job.getEnvironmentAddress());
            SSHConnection ssh = new SSHConnection(env);
            reachable = ssh.isHostReachable();

            this.addJobResultItem(
                    "Connectivity Test",
                    new ArrayList<>(),
                    (reachable) ? ResultType.VALID : ResultType.INVALID,
                    0
            );

        // The Local Environment, i.e. the system on which COMET-Portable is running, is always reachable
        } else if(this.job.isLocalEnvironment()) {
            this.addJobResultItem(
                    "Connectivity Test",
                    new ArrayList<>(),
                    ResultType.VALID,
                    0);
        }

    }

    private void addJobResultItem(String executor, List<String> messages, ResultType type, int jobResultIndex) {

        JobResult jobResult = this.results.get(jobResultIndex);
        JobResultItem item = new JobResultItem();
        item.setJobResult(jobResult);
        item.setExecutor(executor);
        item.setExecutor_message(String.join("\n\r", messages));
        item.setType(type);

        jobResult.addItem(item);
        this.results.set(jobResultIndex, jobResult);
    }

    private InspecCommand getInspecCommandRunner() {
        return new InspecCommand();
    }

}