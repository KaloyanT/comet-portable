package de.cmt.cometportable.util;

import de.cmt.cometportable.test.domain.JobStringConstants;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;

@Component
public class JobMonitoringService {

    // count only the unzipped ones, i.e. the ones in the /jobs directory
    private List<String> downloadedJobs;

    private List<String> runningJobs;

    private List<String> finishedJobs;

    private JobMonitoringService() {

    }

    @PostConstruct
    private void init() {

        this.downloadedJobs = new ArrayList<>();
        this.runningJobs = new ArrayList<>();
        this.finishedJobs = new ArrayList<>();

        Path path = Paths.get(JobStringConstants.getJobsDir());
        Stream<Path> dirs = null;

        try {
            dirs = Files.list(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dirs.forEach(e -> {

            this.addDownloadedJob(e.toString());

            long jobId = Long.parseLong(e.toString().split(".job.")[1]);
            Path temp = Paths.get(e.toString() + "/" + String.format(JobStringConstants.getJobResultFile(), jobId));

            if(Files.exists(temp)) {
                this.addFinishedJob(e.toString());
            }
        });

    }

    public List<String> getDownloadedJobs() {
        return downloadedJobs;
    }

    public void setDownloadedJobs(List<String> downloadedJobs) {
        this.downloadedJobs = downloadedJobs;
    }

    public void addDownloadedJob(String job) {
        this.downloadedJobs.add(job);
    }

    public void removeDownloadedJob(String job) {
        this.downloadedJobs.remove(job);
    }

    public List<String> getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(List<String> runningJobs) {
        this.runningJobs = runningJobs;
    }

    public void addRunningJob(String job) {
        this.runningJobs.add(job);
    }

    public void removeRunningJob(String job) {
        this.runningJobs.remove(job);
    }

    public List<String> getFinishedJobs() {
        return finishedJobs;
    }

    public void setFinishedJobs(List<String> finishedJobs) {
        this.finishedJobs = finishedJobs;
    }

    public void addFinishedJob(String job) {
        this.finishedJobs.add(job);
    }

    public void removeFinishedJob(String job) {
        this.finishedJobs.remove(job);
    }
}
