package de.cmt.cometportable.test.domain;

public class JobStringConstants {

    private final static String JOBS_DIR = "jobs";

    private final static String DOWNLOADS_DIR = "downloads";

    private final static String CUSTOMER_PROJECT_JOB_DIR = "de.cmt.domain.entity.artifact.CustomerProject.job.";

    private final static String JOB_CONFIG_FILE = "job.json";

    private final static String JOB_RESULT_FILE = "result.%d.json";

    public static String getJobsDir() {
        return JOBS_DIR;
    }

    public static String getDownloadsDir() {
        return DOWNLOADS_DIR;
    }

    public static String getCustomerProjectJobDir() {
        return CUSTOMER_PROJECT_JOB_DIR;
    }

    public static String getJobConfigFile() {
        return JOB_CONFIG_FILE;
    }

    public static String getJobResultFile() {
        return JOB_RESULT_FILE;
    }
}
