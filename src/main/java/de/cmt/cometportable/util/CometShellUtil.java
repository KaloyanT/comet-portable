package de.cmt.cometportable.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobResult;
import de.cmt.cometportable.test.domain.JobStringConstants;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class CometShellUtil {

    private final Logger log = LoggerFactory.getLogger(CometShellUtil.class);

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

    public String listJobsFromJobMonitoringService(List<String> jobs) {

        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < jobs.size(); i++) {

            // Each entry will be in the form jobs/de.cmt.domain.entity.artifact.CustomerProject.job.xxxx.
            // Use split to get only the ID
            String id = jobs.get(i).split(".job.")[1];
            stringBuilder.append("Job ");
            stringBuilder.append(id);
            stringBuilder.append((i == jobs.size() - 1) ? "" : "\n");
        }

        return stringBuilder.toString();
    }

    public Job createJob(Long jobId) {

        log.info("Reading configuration for Job {}", jobId);

        if(this.jobExists(jobId) == false) {
            log.error("Job {} doesn't exist!", jobId);
            return null;
        }

        String jobConfigFilePath = JobStringConstants.getJobsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + jobId + "/"
                + JobStringConstants.getJobConfigFile();
        File configFile = new File(jobConfigFilePath);

        ObjectMapper mapper = new ObjectMapper();
        Job job = null;

        try {
            job = mapper.readValue(configFile, Job.class);
        } catch (IOException e) {
            log.error("Invalid Configuration for Job {}", jobId);
        }

        return job;
    }

    public List<JobResult> getJobResults(Long jobId) {

        log.info("Reading results for Job {}", jobId);

        String resultsFilePath = JobStringConstants.getJobsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + jobId + "/"
                + String.format(JobStringConstants.getJobResultFile(), jobId);
        File jobResultsFile = new File(resultsFilePath);

        if(!jobResultsFile.exists()) {
            log.error("Results for Job {} don't exist!", jobId);
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        List<JobResult> jobResults = null;

        try {
            jobResults = Arrays.asList(mapper.readValue(jobResultsFile, JobResult[].class));
        } catch (IOException e) {
            log.error("Invalid Results file for Job {}", jobId);
        }

        return jobResults;
    }

    public void saveJobResults(Job job) {

        log.info("Saving Job results for Job {} to file", job.getId());

        if(job == null || job.getResults() == null || job.getResults().isEmpty()) {
            return;
        }

        String jobResultFile = JobStringConstants.getJobsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + job.getId() + "/"
                + String.format(JobStringConstants.getJobResultFile(), job.getId());

        ObjectMapper mapper = new ObjectMapper();
        List<JobResult> jobResults = job.getResults();

        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        try {
            writer.writeValue(new File(jobResultFile), jobResults);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        log.info("Saving Job results for Job {} to file COMPLETE", job.getId());
    }

    public void unzipJobFiles(Long jobId, File jobFile) {

        log.info("Unzipping ZIP file for Job {}", jobId);

        if(jobFile == null || !jobFile.exists()) {
            log.error("ZIP file for Job {} doesn't exist", jobId);
            return;
        }

        if(this.jobExists(jobId) == true) {
            log.error("Job Directory for Job {} already exists", jobId);
            return;
        }

        // Taken from: https://stackoverflow.com/questions/9324933/what-is-a-good-java-library-to-zip-unzip-files
        String jobDirectory = JobStringConstants.getJobsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + jobId;

        try {
            ZipFile zipFile = new ZipFile(jobFile.getPath());
            zipFile.extractAll(jobDirectory);
        } catch (ZipException e) {
            log.error("Cannot open ZIP file for Job {}", jobId);
            return;
        }

        log.info("Unzipping ZIP file for Job {} COMPLETE", jobId);
    }

    private boolean jobExists(Long jobId) {

        String jobConfigFilePath = JobStringConstants.getJobsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + jobId + "/"
                + JobStringConstants.getJobConfigFile();
        File configFile = new File(jobConfigFilePath);

        if(configFile.exists()) {
            return true;
        }

        return false;
    }
}
