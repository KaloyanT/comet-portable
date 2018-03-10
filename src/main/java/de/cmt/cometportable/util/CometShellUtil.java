package de.cmt.cometportable.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.test.domain.Job;
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


    public String listExportedJobs(List<ObjectNode> jobs) {

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < jobs.size(); i++) {

            ObjectNode node = jobs.get(i);

            if(node.has("id")) {
                stringBuilder.append("Job " + node.get("id") + (i == jobs.size() - 1 ? "" : "\n"));
            }
        }

        return stringBuilder.toString();
    }

    public Job createJob(Long jobId) {

        log.info("Reading configuration for Job {}", jobId);

        String jobDirectory = JOBS_DIR + "/" + CUSTOMER_PROJECT_JOB_DIR + jobId + "/" + JOB_CONFIG_FILE;
        File jsonFile = new File(jobDirectory);

        if(jsonFile == null || !jsonFile.exists()) {
            log.error("Job {} doesn't exist!", jobId);
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        Job job = null;

        try {
            job = mapper.readValue(jsonFile, Job.class);
        } catch (IOException e) {
            log.error("Invalid JSON for Job {}", jobId);
        }

        return job;
    }

    public String getJobResult(Long jobId) {

        return "temp";
    }
}
