package de.cmt.cometportable.test.domain.report;

import de.cmt.cometportable.test.domain.ResultType;

import java.util.List;
import java.util.ArrayList;

public class ParsedTestResult {

    private String testId;

    private List<ParsedTestResultItem> items;

    private ResultType resultType;

    public ParsedTestResult() {
        this.testId = "";
        this.items = new ArrayList<>();
        this.resultType = ResultType.UNKNOWN;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public List<ParsedTestResultItem> getItems() {
        return items;
    }

    public void setItems(List<ParsedTestResultItem> items) {
        this.items = items;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
}