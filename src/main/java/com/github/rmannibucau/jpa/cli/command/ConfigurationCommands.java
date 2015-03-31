package com.github.rmannibucau.jpa.cli.command;

import com.github.rmannibucau.jpa.cli.configuration.Configuration;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;

import java.io.File;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;

@Command("setup")
public class ConfigurationCommands {
    @Command("table")
    public void table(@Option("format") @Required final Configuration.Output output) {
        cli().getConfiguration().setOutput(output);
    }

    @Command("alias")
    public void alias(@Option("load") @Required final File source) {
        cli().getAliases().load(source);
    }

    @Command("set-alias")
    public void alias(@Option("alias") @Required final String name, @Option({ "command", "cmd" }) final String cmd) {
        cli().getAliases().register(name, cmd);
    }

    @Command("remove-alias")
    public void removeAlias(@Option("alias") @Required final String name) {
        cli().getAliases().deregister(name);
    }

    @Command("clear-aliases")
    public void clearAliases(@Option("alias") @Required final String name) {
        cli().getAliases().clear();
    }
}
