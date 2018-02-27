package de.cmt.cometportable.test.plugin.inspec.execution;

import org.apache.commons.exec.LogOutputStream;
import org.apache.log4j.Level;

import java.util.LinkedList;
import java.util.List;

public class InspecOutput extends LogOutputStream {

    private final List<String> lines = new LinkedList<String>();
    private final List<String> errorLines = new LinkedList<String>();

    public InspecOutput(Level logLevel) {
        super(logLevel.toInt());
    }

    @Override
    protected void processLine(String line, int level) {
        lines.add(line);
    }

    public List<String> getLines() {
        return lines;
    }

    public List<String> getErrorLines() {
        return errorLines;
    }

}
