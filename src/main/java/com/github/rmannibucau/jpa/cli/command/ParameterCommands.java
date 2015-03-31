package com.github.rmannibucau.jpa.cli.command;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;

@Command("parameter")
public class ParameterCommands {
    @Command("clean")
    public static void clean() {
        cli().getParameters().clear();
    }

    @Command("remove")
    public static void remove(@Option({ "name", "n" }) final String name,
                              @Option({"position", "p"}) @Default("-1") final int position) {
        cli().getParameters().deregister(name, position);
    }

    @Command("add")
    public static void register(@Option({ "name", "n" }) final String name,
                                @Option({"position", "p"}) @Default("-1") final int position,
                                @Option({"type", "t"}) @Default("string")final String type,
                                @Option({"value", "v"}) final String value,
                                @Option({"pattern"}) final String pattern) {
        cli().getParameters().register(name, position, type, value, pattern);
    }
}
