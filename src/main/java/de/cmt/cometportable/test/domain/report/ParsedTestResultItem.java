package de.cmt.cometportable.test.domain.report;

import de.cmt.cometportable.test.domain.ResultType;

public class ParsedTestResultItem {

    private String description;

    private String resultDescription;

    private ResultType resultType;

    public ParsedTestResultItem() {
        this.description = "";
        this.resultDescription = "";
        this.resultType = ResultType.UNKNOWN;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public void setResultDescription(String resultDescription) {
        this.resultDescription = resultDescription;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }
}
