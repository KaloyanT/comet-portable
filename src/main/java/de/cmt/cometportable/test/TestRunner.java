package de.cmt.cometportable.test;

import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;

import java.io.File;

public interface TestRunner {

    /**
     * Configuration methods
     */
    public TestRunner setExportDestination(File destination);

    public JobResult execute(Job job);

    public JobResult getResult();
}


