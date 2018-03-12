package de.cmt.cometportable.util;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class CometShellUtil {

    private final Logger log = LoggerFactory.getLogger(CometShellUtil.class);

    private final static String JOBS_DIR = "jobs";

    private final static String CUSTOMER_PROJECT_JOB_DIR = "de.cmt.domain.entity.artifact.CustomerProject.job.";

    private final static String JOB_CONFIG_FILE = "job.json";

    private final static String JOB_RESULT_FILE = "result.json";


    public String listExportedJobs(List<ObjectNode> jobs) {

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < jobs.size(); i++) {

            ObjectNode node = jobs.get(i);

            if(node.has("id")) {
                stringBuilder.append("Job ");
                stringBuilder.append(node.get("id"));
                stringBuilder.append((i == jobs.size() - 1) ? "" : "\n");
            }
        }

        return stringBuilder.toString();
    }

    public Job createJob(Long jobId) {

        log.info("Reading configuration for Job {}", jobId);

        String jobConfigFilePath = JOBS_DIR + "/" + CUSTOMER_PROJECT_JOB_DIR + jobId + "/" + JOB_CONFIG_FILE;
        File configFile = new File(jobConfigFilePath);

        if(!configFile.exists()) {
            log.error("Job {} doesn't exist!", jobId);
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        Job job = null;

        try {
            job = mapper.readValue(configFile, Job.class);
        } catch (IOException e) {
            log.error("Invalid Configuration for Job {}", jobId);
        }

        return job;
    }

    public JobResult getJobResult(Long jobId) {

        log.info("Reading results for Job {}", jobId);

        String resultsFilePath = JOBS_DIR + "/" + CUSTOMER_PROJECT_JOB_DIR + jobId + "/" + JOB_RESULT_FILE;
        File resultsFile = new File(resultsFilePath);

        if(!resultsFile.exists()) {
            log.error("Results for Job {} don't exist!", jobId);
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JobResult jobResult = null;

        try {
            jobResult = mapper.readValue(resultsFile, JobResult.class);
        } catch (IOException e) {
            log.error("Invalid Results file for Job {}", jobId);
        }

        return jobResult;
    }

    public void saveJobResults(Job job) {

        log.info("Saving Job results for Job {} to file", job.getId());

        if(job == null || job.getResult() == null || job.getResult().getItems().isEmpty()) {
            return;
        }

        String jobResultFile = JOBS_DIR + "/" + CUSTOMER_PROJECT_JOB_DIR + job.getId() + "/" + JOB_RESULT_FILE;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jobResult = mapper.valueToTree(job.getResult());

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        try {
            writer.writeValue(new File(jobResultFile), jobResult);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        log.info("Saving Job results for Job {} to file COMPLETE", job.getId());
    }
}
