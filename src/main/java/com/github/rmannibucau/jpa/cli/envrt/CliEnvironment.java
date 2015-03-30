package com.github.rmannibucau.jpa.cli.envrt;

import com.github.rmannibucau.jpa.cli.Cli;
import org.tomitribe.crest.environments.SystemEnvironment;

public class CliEnvironment extends SystemEnvironment {
    private final Cli cli;

    public CliEnvironment(final Cli cli) {
        this.cli = cli;
    }

    public Cli getCli() {
        return cli;
    }
}
