package com.github.rmannibucau.jpa.cli.command;

import com.github.rmannibucau.jpa.cli.Cli;
import com.github.rmannibucau.jpa.cli.envrt.CliEnvironment;
import org.tomitribe.crest.environments.Environment;

public final class Commands {
    public static Cli cli() {
        return CliEnvironment.class.cast(Environment.ENVIRONMENT_THREAD_LOCAL.get()).getCli();
    }

    private Commands() {
        // no-op
    }
}
