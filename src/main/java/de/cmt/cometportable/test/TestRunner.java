package de.cmt.cometportable.test;

import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;

import java.io.File;
import java.util.List;

public interface TestRunner {

    /**
     * Configuration methods
     */
    public TestRunner setExportDestination(File destination);

    public List<JobResult> execute(Job job);

    public List<JobResult> getResults();
}


