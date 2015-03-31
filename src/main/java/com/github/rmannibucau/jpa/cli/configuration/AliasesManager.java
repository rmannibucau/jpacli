package com.github.rmannibucau.jpa.cli.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AliasesManager {
    private final Properties aliases = new Properties();

    public void load(final File file) {
        try {
            try (final FileInputStream fis = new FileInputStream(file)) {
                aliases.load(fis);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public String findOrNoop(final String line) {
        return aliases.containsKey(line) ? aliases.getProperty(line) : line;
    }

    public void register(final String name, final String cmd) {
        aliases.setProperty(name, cmd);
    }

    public void deregister(final String name) {
        aliases.remove(name);
    }

    public void clear() {
        aliases.clear();
    }
}
