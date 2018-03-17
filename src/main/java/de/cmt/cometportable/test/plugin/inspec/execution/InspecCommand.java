package de.cmt.cometportable.test.plugin.inspec.execution;

import de.cmt.cometportable.test.domain.Environment;
import de.cmt.cometportable.test.domain.EnvironmentAuthenticationType;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.Job.EnvironmentType;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InspecCommand extends CommandLine {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Executor executor = new DefaultExecutor();

    protected Map<Job.EnvironmentType, String> envProtocol = new HashMap<>();

    // https://stackoverflow.com/questions/37116062/error-trying-to-open-cmd-using-apache-commons-exec-jar
    // On Windows one has to insert "cmd.exe /c" before the actual command. Apache Commons Exec doesn't do
    // that by default. If "cmd.exe /c" is not inserted, the command/process that one wants to execute
    // won't be found
    private static String inspecCommand = SystemUtils.IS_OS_WINDOWS ? "cmd.exe" : "inspec";

    public InspecCommand() {

        this(inspecCommand);

        if(SystemUtils.IS_OS_WINDOWS) {
            this.addArgument("/c");
            this.addArgument("inspec");
        }

        this.log.info("Command created");
    }

    public InspecCommand(String inspecCommand) {

        super(inspecCommand);

        this.envProtocol.put(EnvironmentType.LOCAL, "");
        this.envProtocol.put(EnvironmentType.SSH, "ssh://");
        this.envProtocol.put(EnvironmentType.DOCKER, "docker://");
        this.envProtocol.put(EnvironmentType.WINRM, "winrm://");
    }

    public InspecOutput execute() {

        this.log.info("InSpec Command execution started ... ");
        this.log.info("Running command {} {} ", this.getExecutable(), String.join(" ", this.getArguments()));


        InspecOutput logHandler = new InspecOutput(Level.DEBUG);
        InspecOutput errorHandler = new InspecOutput(Level.ERROR);
        PumpStreamHandler pump = new PumpStreamHandler(logHandler, errorHandler);
        InspecResult resultHandler = new InspecResult(logHandler);

        executor.setStreamHandler(pump);

        // The Inspec process exits successfully with 0 on Windows.
        // Not tested/debugged on Linux to see if it actually exits with 1
        if(SystemUtils.IS_OS_WINDOWS) {
            executor.setExitValue(0);
        } else {
            executor.setExitValue(1); //when used async set it to 1
        }

        try {
            // removed resultHandler to get a blocking call...
            // since this command is run in an own thread, we can omit async here
            executor.execute(this, resultHandler);
        } catch(IOException e) {
            this.log.error("InSpec Command execution failed");
            this.log.error(e.getMessage());
            this.log.error("Exception", e);
        }

        try {
            resultHandler.waitFor();
        } catch(InterruptedException e) {
            this.log.error("Inspec Command was interrupted by system");
            this.log.error(e.getMessage());
            this.log.error("Exception", e);
            Thread.currentThread().interrupt();
        }

        if(errorHandler.getLines().size() > 0) {
            log.error("Inspec Command errors occurred: ");
            for(String line : errorHandler.getLines()) {
                this.log.error(line);
            }
        }

        return logHandler;
    }

    protected InspecCommand addEnvironmentArgument(EnvironmentType type, String address) {
        this.addArgument("--target");
        this.addArgument(this.envProtocol.get(type).concat(address));

        return this;
    }

    protected InspecCommand addEnvironmentArgument(Environment environment) {

        String connection = "";

        if(environment.getAuthenticationType() == EnvironmentAuthenticationType.PASSWORD) {

            connection = this.envProtocol.get(EnvironmentType.SSH)
                    .concat(environment.getUser())
                    .concat(":")
                    .concat(environment.getPassword())
                    .concat("@")
                    .concat(environment.getHost());

        } else if(environment.getAuthenticationType() == EnvironmentAuthenticationType.WINRM_PASSWORD) {

            connection = this.envProtocol.get(EnvironmentType.WINRM)
                    .concat(environment.getUser())
                    .concat("@")
                    .concat(environment.getHost());
            //.concat(" --password '" + environment.getPassword() + "'");

        } else {

            connection = this.envProtocol.get(EnvironmentType.SSH)
                    .concat(environment.getUser())
                    .concat("@")
                    .concat(environment.getHost());

            this.addArgument("-i");
            this.addArgument(environment.getKeyFile());
        }

        this.addArgument("-t");
        this.addArgument(connection);

        if(environment.getAuthenticationType() == EnvironmentAuthenticationType.WINRM_PASSWORD) {
            this.addArgument("--password");
            this.addArgument(environment.getPassword());
        }
        return this;
    }

    protected InspecCommand addFormatArgument() {
        return this.addFormatArgument("json-min");
    }

    protected InspecCommand addExecutionArgument() {
        this.addArgument("exec");
        this.addArgument("--no-color");
        return this;
    }

    protected InspecCommand addFormatArgument(String format) {
        this.addArgument("--format");
        this.addArgument(format);
        return this;
    }

    public InspecOutput version() {
        this.addArgument("version");
        return this.execute();
    }

    public InspecOutput help() {
        this.addArgument("help");
        return this.execute();
    }

    public InspecOutput check(File profileFolder) {
        this.addCheckArgument();
        this.addArgument(profileFolder.getPath());
        return this.execute();
    }

    public InspecCommand addCheckArgument() {
        this.addArgument("check");
        this.addFormatArgument("json");
        return this;
    }

    public InspecOutput test(File profileFolder) {
        this.addExecutionArgument();
        this.addFormatArgument();

        this.addArgument(profileFolder.getPath());
        return this.execute();
    }

    public InspecOutput test(File profileFolder, EnvironmentType type, String address) {
        this.addExecutionArgument();
        this.addEnvironmentArgument(type, address);
        this.addFormatArgument();
        this.addArgument(profileFolder.getPath());

        return this.execute();
    }

    public InspecOutput test(File profileFolder, Environment environment) {
        this.addExecutionArgument();
        this.addEnvironmentArgument(environment);
        this.addFormatArgument();
        this.addArgument(profileFolder.getPath());

        return this.execute();
    }

    public String getCommandString() {
        return this.getExecutable() + " "+ String.join(" ", this.getArguments());
    }
}

