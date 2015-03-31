package com.github.rmannibucau.jpa.cli.command;

import com.github.rmannibucau.jpa.cli.Cli;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.apache.xbean.finder.filter.Filter;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;
import static java.util.Arrays.asList;
import static org.apache.xbean.finder.archive.ClasspathArchive.archive;

@Command("info")
public class InfoCommands {
    @Command("auto")
    public static void auto(@Option("embeddable") @Default("true") final boolean embeddable,
                            @Option("mapped") @Default("true") final boolean mapped) {
        autoEntities(embeddable, mapped);
        cli().reset();
    }

    private static void autoEntities(final boolean embeddable, final boolean mapped) {
        final Cli cli = cli();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Collection<URL> urls = new UrlSet(contextClassLoader)
                    .excludeJvm()
                    .filter(new Filter() {
                        @Override
                        public boolean accept(final String name) {
                            final String realName;
                            try {
                                if (name.startsWith("file:")) {
                                    realName = new File(new URL(name).getFile()).getName();
                                } else if (name.startsWith("jar:")) {
                                    return accept(name.substring("jar:".length()));
                                } else {
                                    realName = null;
                                }
                            } catch (final Exception e) {
                                return true;
                            }
                            return !(realName != null && realName.endsWith(".jar")
                                    && (realName.startsWith("idea_rt")
                                    || realName.startsWith("jpacli-")
                                    || realName.startsWith("eclipse")
                                    || realName.startsWith("hibernate")
                                    || realName.startsWith("openjpa-")
                                    || realName.startsWith("javax")
                                    || realName.startsWith("javaee")
                                    || realName.startsWith("tomitribe-")
                                    || realName.startsWith("xbean-")));
                        }
                    })
                    .getUrls();
            final Collection<Archive> archives = new LinkedList<>();
            for (final URL url : urls) {
                archives.add(archive(contextClassLoader, url));
            }
            final AnnotationFinder finder = new AnnotationFinder(new CompositeArchive(archives));
            for (final Class<?> e : finder.findAnnotatedClasses(Entity.class)) {
                cli.getInfo().getManagedClassNames().add(e.getName());
            }
            if (embeddable) {
                for (final Class<?> e : finder.findAnnotatedClasses(Embeddable.class)) {
                    cli.getInfo().getManagedClassNames().add(e.getName());
                }
            }
            if (mapped) {
                for (final Class<?> e : finder.findAnnotatedClasses(MappedSuperclass.class)) {
                    cli.getInfo().getManagedClassNames().add(e.getName());
                }
            }
        } catch (final IOException e) {
            // no-op
        }
    }

    @Command("set-provider")
    public static void setProvider(@Option({"provider", "p"}) final String provider) {
        final Cli cli = cli();
        cli.getInfo().setProviderClassName(provider);
        cli.reset();
    }

    @Command("set-datasource")
    public static void setDataSource(@Option({"datasource", "ds", "d"}) final String datasource) {
        final Cli cli = cli();
        cli.getInfo().setDataSource(cli.getDataSourceProvider().getDataSource(datasource));
        cli.reset();
    }

    @Command("set-property")
    public static void setProperty(@Option({"key", "k"}) final String key, @Option({"value", "v"}) final String value) {
        final Cli cli = cli();
        cli.getInfo().getProperties().setProperty(key, value == null ? "" : value);
        cli.reset();
    }

    @Command("clear-property")
    public static void clearProperty(@Option({"key", "k"}) final String key) {
        final Cli cli = cli();
        cli.getInfo().getProperties().remove(key);
        cli.reset();
    }

    @Command("entity")
    public static void addEntity(@Option({"class", "c"}) final String clazz) {
        final Cli cli = cli();
        cli.getInfo().getManagedClassNames().add(clazz);
        cli.reset();
    }

    @Command("remove-entity")
    public static void removeEntity(@Option({"class", "c"}) final String clazz) {
        final Cli cli = cli();
        cli.getInfo().getManagedClassNames().remove(clazz);
        cli.reset();
    }

    @Command("clear-entities")
    public static void removeEntities(@Option({"class", "c"}) final String clazz) {
        final Cli cli = cli();
        cli.getInfo().getManagedClassNames().clear();
        cli.reset();
    }

    @Command("load")
    public static void load(@Option({"file", "f"}) final File from) {
        final Cli cli = cli();
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
            if (key.endsWith(".entities")) {
                final String value = properties.getProperty(key);
                if ("auto".equals(value)) {
                    auto(true, true);
                } else {
                    cli.getInfo().getManagedClassNames().addAll(asList(value.split(" *, *")));
                }
            } else if (key.endsWith(".datasource")) {
                final String value = properties.getProperty(key);
                cli.getInfo().setDataSource(cli.getDataSourceProvider().getDataSource(value));
            }
        }
    }
}
