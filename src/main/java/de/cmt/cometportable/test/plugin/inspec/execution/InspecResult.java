package de.cmt.cometportable.test.plugin.inspec.execution;

import org.apache.commons.exec.DefaultExecuteResultHandler;

public class InspecResult extends DefaultExecuteResultHandler {

    private InspecOutput outputStream;

    public InspecResult(InspecOutput stream) {
        this.outputStream = stream;
    }

    public InspecOutput getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(InspecOutput outputStream) {
        this.outputStream = outputStream;
    }

}
