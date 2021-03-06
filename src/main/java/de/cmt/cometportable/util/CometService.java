package de.cmt.cometportable.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.cmt.cometportable.test.domain.Job;
import de.cmt.cometportable.test.domain.JobStringConstants;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CometService {

    private final Logger log = LoggerFactory.getLogger(CometService.class);

    @Value("${comet.url}")
    private String COMET_URL;

    @Value("${comet.port}")
    private String COMET_PORT;

    private static String COMET_ADDRESS;

    @Value("${comet.authentication.endpoint}")
    private String COMET_AUTHENTICATION_ENDPOINT;

    private final static String usernameField = "j_username";

    @Value("${comet.authentication.username}")
    private String usernameFieldValue;

    private final static String passwordField = "j_password";

    @Value("${comet.authentication.password}")
    private String passwordFieldValue;

    private final static String rememberMeField = "remember-me";

    @Value("${comet.authentication.remember-me}")
    private String rememberMeFieldValue;

    private final static String submitField = "submit";

    @Value("${comet.authentication.submit}")
    private String submitFieldValue;

    private static String JSSESIONID;

    @Autowired
    public CometService() {

    }

    @PostConstruct
    private void init() {
        // Establish connection to COMET
        COMET_ADDRESS = COMET_URL + ":" + COMET_PORT;
        this.authenticate();
    }

    // Run every hour in order to refresh Token/Cookie
    @Scheduled(cron = "0 0 * * * ?")
    public void authenticate() {

        log.info("Trying to authenticate with COMET");

        ResponseEntity<String> response;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Taken from: https://stackoverflow.com/questions/38372422/how-to-post-form-data-with-spring-resttemplate
        MultiValueMap<String, String> loginMap = new LinkedMultiValueMap<>();
        loginMap.add(usernameField, usernameFieldValue);
        loginMap.add(passwordField, passwordFieldValue);
        loginMap.add(rememberMeField, rememberMeFieldValue);
        loginMap.add(submitField, submitFieldValue);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(loginMap, headers);

        String authenticationUrl = COMET_ADDRESS + COMET_AUTHENTICATION_ENDPOINT;

        try {
            response = restTemplate.exchange(authenticationUrl, HttpMethod.POST, entity, String.class);
        }  catch(HttpClientErrorException e) {
            log.error(e.getMessage());
            return;
        } catch (RestClientException re) {
            // No Connection to COMET
            log.error(re.getMessage());
            return;
        }

        if(response != null && response.getStatusCode() == HttpStatus.OK) {

            HttpHeaders responseHeaders = response.getHeaders();

            if(responseHeaders.containsKey(HttpHeaders.SET_COOKIE)) {
                JSSESIONID = responseHeaders.getFirst(HttpHeaders.SET_COOKIE);
                log.debug("JSSESION ID SET");
            }
        }

        log.info("Authentication attempt with COMET COMPLETE");

    }

    public List<ObjectNode> getExportedJobs() {

        log.debug("Request to get all exported Jobs from COMET");

        if(JSSESIONID == null || JSSESIONID.isEmpty()) {
            log.error("No JSSESIONID available. Can't connect to COMET");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, JSSESIONID);

        RestTemplate restTemplate = new RestTemplate();
        List<ObjectNode> exportedJobsList = null;
        final String url = COMET_ADDRESS + "/api/job/get/exported";

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ObjectNode[]> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, ObjectNode[].class);
        } catch(HttpClientErrorException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        } catch (RestClientException re) {
            // No Connection
            log.error(re.getMessage());
            return null;
        }

        if(response != null) {
            exportedJobsList = new ArrayList<>(Arrays.asList(response.getBody()));
        }

        log.debug("Request to get all exported Jobs from COMET COMPLETE");

        return exportedJobsList;
    }

    public void importJobResults(Job job) {

        log.info("Importing the test results for Job {}", job.getId());

        if(JSSESIONID == null || JSSESIONID.isEmpty()) {
            log.error("No JSSESIONID available. Can't connect to COMET");
            return;
        }

        if(job.getResults() == null | job.getResults().isEmpty()) {
            log.error("Invalid Job Results for Job {}", job.getId());
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.COOKIE, JSSESIONID);

        RestTemplate restTemplate = new RestTemplate();
        final String url = COMET_ADDRESS + "/api/job/" + job.getId() + "/import/test-results";

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode json = mapper.valueToTree(job.getResults());

        HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        }  catch(HttpClientErrorException e) {
            log.error(e.getMessage());
            return;
        } catch (RestClientException re) {
            // No Connection to COMET
            log.error(re.getMessage());
            return;
        }

        if(response.getStatusCode() != HttpStatus.CREATED) {
            log.error("Cannot import Results for this Job. Job is already marked as FINISHED by COMET");
        }

        log.info("Import of test results COMPLETE");
    }

    public File downloadJobAsZIP(Long jobId) {

        log.info("Downloading Job {} as ZIP", jobId);

        if(JSSESIONID == null || JSSESIONID.isEmpty()) {
            log.error("No JSSESIONID available. Can't connect to COMET");
            return null;
        }

        // Taken from: https://stackoverflow.com/questions/35995431/how-to-specify-user-agent-and-referer-in-fileutils-copyurltofileurl-file-meth
        String url = COMET_ADDRESS + "/api/job/" + jobId + "/export/zip";
        String fileName = JobStringConstants.getDownloadsDir() + "/"
                + JobStringConstants.getCustomerProjectJobDir() + jobId + ".zip";

        File jobFile = new File(fileName);

        if(jobFile.exists()) {
            log.error("Job {} is already downloaded", jobId);
            return jobFile;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader(HttpHeaders.COOKIE, JSSESIONID);
        boolean downloadSuccessful = true;

        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            org.apache.http.HttpEntity httpEntity = httpResponse.getEntity();

            if(httpEntity != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                FileUtils.copyInputStreamToFile(httpEntity.getContent(), jobFile);
            } else {
                downloadSuccessful = false;
            }

        } catch (IOException e) {
            log.error("Cannot download Job {}", jobId);
            downloadSuccessful = false;
        } finally {
            httpGet.releaseConnection();
        }

        if(downloadSuccessful == false) {
            return null;
        }

        log.info("Downloading Job {} as ZIP COMPLETE", jobId);

        return jobFile;
    }
}
