package de.cmt.cometportable.shell;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CometShell {

    @ShellMethod("Add two integers")
    public int add(int a, int b) {
        return a + b;
    }

    @ShellMethod("Executes some command")
    public String run(String parameter, String value) {
        return parameter + value;
    }
}
