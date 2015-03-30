package com.github.rmannibucau.jpa.cli.command;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;

@Command("datasource")
public class DataSourceCommands {
    @Command("register")
    public void register(@Option({ "name", "n" }) @Default("default") final String name,
                         @Option({ "url", "u" }) final String url,
                         @Option({ "user" }) final String user,
                         @Option({ "password", "pwd", "p" }) final String password) {
        cli().getDataSourceProvider().register(name, url, user, password);
    }

    @Command("deregister")
    public void deregister(@Option({ "name", "n" }) final String name) {
        cli().getDataSourceProvider().deregister(name);
    }

    @Command("load-driver")
    public void driver(@Option({ "class", "c" }) final String clazz) {
        try {
            Class.forName(clazz, true, Thread.currentThread().getContextClassLoader());
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Command("load")
    public void load(@Option({ "file", "f" }) final File from) {
        if (!from.isFile()) {
            return;
        }

        final Properties properties = new Properties();
        try (final FileInputStream fis = new FileInputStream(from)) {
            properties.load(fis);
        } catch (final IOException e) {
            e.printStackTrace();
            return;
        }

        for (final String key : properties.stringPropertyNames()) {
            if (key.endsWith(".url")) {
                final String prefix = key.substring(0, key.length() - ".url".length());
                final String url = properties.getProperty(key);
                final String name = properties.getProperty(prefix + ".name", properties.getProperty(prefix + ".id", prefix));
                final String user = properties.getProperty(prefix + ".username", properties.getProperty(prefix + ".user"));
                final String pwd = properties.getProperty(prefix + ".passord");
                final String driver = properties.getProperty(prefix + ".driver");

                register(name, url, user, pwd);
                if (driver != null) {
                    try {
                        Class.forName(driver, true, Thread.currentThread().getContextClassLoader());
                    } catch (final ClassNotFoundException e) {
                        // no-op
                    }
                }
            }
        }
    }
}
