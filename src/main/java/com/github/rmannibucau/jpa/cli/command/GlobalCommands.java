package com.github.rmannibucau.jpa.cli.command;

import com.github.rmannibucau.jpa.cli.Cli;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;

public class GlobalCommands {
    @Command("script")
    public static void script(@Option({ "file", "f" }) final File script) {
        if (!script.exists()) {
            return;
        }

        String line;
        final Cli cli = cli();
        try (final BufferedReader reader = new BufferedReader(new FileReader(script))) {
            while ((line = reader.readLine()) != null) {
                cli.execute(line);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
