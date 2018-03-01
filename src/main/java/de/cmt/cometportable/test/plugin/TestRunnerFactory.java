package de.cmt.cometportable.test.plugin;

import de.cmt.cometportable.test.TestRunner;
import de.cmt.cometportable.test.plugin.inspec.InspecTestRunner;

public class TestRunnerFactory {

    public static final TestRunner getRunner() {
        return new InspecTestRunner();
    }
}

