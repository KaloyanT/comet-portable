package de.cmt.cometportable.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CometService {

    private final Logger log = LoggerFactory.getLogger(CometService.class);

    private final static String usernameField = "j_username";

    private final static String passwordField = "j_password";

    private final static String rememberMeField = "remember-me";

    private final static String submitField = "submit";

    private final static String authenticationUrl = "http://localhost:8080/api/authentication";

    private static String JSSESIONID;

    @Autowired
    public CometService() {

    }

    @PostConstruct
    private void init() {
        // Establish connection to COMET
        this.authenticate();
    }

    private void authenticate() {

        log.debug("Trying to authenticate with COMET");

        ResponseEntity<String> response = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Taken from: https://stackoverflow.com/questions/38372422/how-to-post-form-data-with-spring-resttemplate
        MultiValueMap<String, String> loginMap = new LinkedMultiValueMap<String, String>();
        loginMap.add(usernameField, "admin");
        loginMap.add(passwordField, "admin");
        loginMap.add(rememberMeField, "undefined");
        loginMap.add(submitField, "Login");

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(loginMap, headers);

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
                String setCookie = responseHeaders.getFirst(HttpHeaders.SET_COOKIE);
                JSSESIONID = setCookie;
                log.debug("JSSESION ID SET");
            }
        }

        log.debug("Authentication attempt with COMET COMPLETE");

    }

    public List<ObjectNode> getExportedJobs() {

        log.debug("Request to get all exported Jobs from COMET");

        if(JSSESIONID == null || JSSESIONID.isEmpty()) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, JSSESIONID);

        RestTemplate restTemplate = new RestTemplate();
        List<ObjectNode> exportedJobsList = null;
        final String url = "http://localhost:8080/api/job/get/exported";

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ObjectNode[]> response = null;

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, ObjectNode[].class);
        } catch(HttpClientErrorException e) {
            log.error(e.getMessage());
            return new ArrayList<ObjectNode>();
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

}
