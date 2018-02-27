package de.cmt.cometportable.test.plugin.inspec.evaluation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cmt.cometportable.test.domain.ResultType;
import de.cmt.cometportable.test.domain.report.ParsedTestResult;
import de.cmt.cometportable.test.domain.report.ParsedTestResultItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InspecEvaluate {

    //properties for json parsing
    private static final String JSON_KEY_CONTROL = "controls";
    private static final String JSON_KEY_STATUS = "status";
    private static final String JSON_KEY_MESSAGE = "message";
    private static final String JSON_KEY_SKIP_MESSAGE = "skip_message";
    private static final String JSON_KEY_SUMMARY = "summary";
    private static final String JSON_VALUE_PASSED = "passed";
    private static final String JSON_VALUE_FAILED = "failed";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private String jsonResult;

    private ResultType result;

    public ResultType getResult() {
        return result;
    }

    public InspecEvaluate(String result) {

        this.jsonResult = result;

        if(StringUtils.isNotEmpty(this.jsonResult)) {
            JsonNode rootNode = this.parse();
            if(rootNode != null) {
                this.result = this.evaluate(rootNode);
            } else {
                this.result = ResultType.UNKNOWN;
                log.error("Result evaluation failed");
            }
        } else {
            this.log.warn("Inspec result input was empty, nothing to evaluate");
            this.result = ResultType.UNKNOWN;
        }

    }

    private JsonNode parse() {

        JsonNode rootNode = null;

        try {
            JsonFactory factory = new JsonFactory();
            ObjectMapper mapper = new ObjectMapper(factory);
            rootNode = mapper.readTree(this.jsonResult);

        } catch(IOException e) {

            this.log.error("Result evaluation failed");
            this.log.error(e.getMessage());
            this.log.error("Exception", e);
        }

        return rootNode;
    }

    private ResultType evaluate(JsonNode rootNode) {

        ResultType res = ResultType.VALID;

        if(rootNode.has(InspecEvaluate.JSON_KEY_CONTROL)) {

            JsonNode controls = rootNode.get(InspecEvaluate.JSON_KEY_CONTROL);

            if(controls.isArray()) {
                for(JsonNode testResult : controls) {
                    //this.log.debug("Check Result node, got status {} ", testResult.get("status").toString());

                    if(!testResult.get(InspecEvaluate.JSON_KEY_STATUS).asText().equals(InspecEvaluate.JSON_VALUE_PASSED)) {
                        res = ResultType.INVALID;
                        //one invalid is enough for now ..
                        break;
                    }
                }
            }

        } else if(rootNode.has(InspecEvaluate.JSON_KEY_SUMMARY)) {
            JsonNode summary = rootNode.get(InspecEvaluate.JSON_KEY_SUMMARY);
            boolean valid = summary.get("valid").asBoolean();
            res = (valid) ? ResultType.VALID : ResultType.INVALID;

        } else {
            this.log.error("Result evaluation failed - field `controls` not found on resultRootNode");
            res = ResultType.INVALID;
        }

        return res;
    }

    public List<ParsedTestResult> getTestResults() {

        JsonNode rootNode = this.parse();

        Map<String, ParsedTestResult> resultLines = new HashMap<>();

        if(rootNode != null && rootNode.has(InspecEvaluate.JSON_KEY_CONTROL)) {

            JsonNode controls = rootNode.get(InspecEvaluate.JSON_KEY_CONTROL);

            if(controls.isArray()) {
                for(JsonNode testResult : controls) {

                    String testId = testResult.get("profile_id").asText();
                    ParsedTestResult parsedTestResult;
                    ParsedTestResultItem resultItem = new ParsedTestResultItem();

                    if(!resultLines.containsKey(testId)){
                        parsedTestResult = new ParsedTestResult();
                        parsedTestResult.setTestId(testId);
                        resultLines.put(testId, parsedTestResult);
                    }

                    parsedTestResult = resultLines.get(testId);
                    resultItem.setDescription(testResult.get("code_desc").asText());

                    this.parseTestStatus(testResult, resultItem);
                    this.parseResultDescription(testResult, resultItem);

                    if(parsedTestResult.getResultType() != ResultType.INVALID) {
                        parsedTestResult.setResultType(resultItem.getResultType());
                    }

                    parsedTestResult.getItems().add(resultItem);
                }
            }
        }

        return resultLines.values().stream().collect(Collectors.toList());
    }

    private void parseTestStatus(JsonNode test, ParsedTestResultItem resultItem) {
        if(test.get(InspecEvaluate.JSON_KEY_STATUS).asText().equals(InspecEvaluate.JSON_VALUE_PASSED)) {
            resultItem.setResultType(ResultType.VALID);

        } else if(test.get(InspecEvaluate.JSON_KEY_STATUS).asText().equals(InspecEvaluate.JSON_VALUE_FAILED)){
            resultItem.setResultType(ResultType.INVALID);

        }
    }

    private void parseResultDescription(JsonNode test, ParsedTestResultItem resultItem) {
        if(test.has(InspecEvaluate.JSON_KEY_MESSAGE)) {
            resultItem.setResultDescription(test.get(InspecEvaluate.JSON_KEY_MESSAGE).asText());
        } else if(test.has(InspecEvaluate.JSON_KEY_SKIP_MESSAGE)) {
            resultItem.setResultDescription(test.get(InspecEvaluate.JSON_KEY_SKIP_MESSAGE).asText());
        }
    }
}
