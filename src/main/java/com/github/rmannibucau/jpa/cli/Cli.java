package com.github.rmannibucau.jpa.cli;

import com.github.rmannibucau.jpa.cli.command.ConfigurationCommands;
import com.github.rmannibucau.jpa.cli.command.DataSourceCommands;
import com.github.rmannibucau.jpa.cli.command.GlobalCommands;
import com.github.rmannibucau.jpa.cli.command.InfoCommands;
import com.github.rmannibucau.jpa.cli.command.JPACommands;
import com.github.rmannibucau.jpa.cli.command.ParameterCommands;
import com.github.rmannibucau.jpa.cli.configuration.AliasesManager;
import com.github.rmannibucau.jpa.cli.configuration.Configuration;
import com.github.rmannibucau.jpa.cli.envrt.CliEnvironment;
import com.github.rmannibucau.jpa.cli.impl.PersistenceUnitInfoImpl;
import com.github.rmannibucau.jpa.cli.jdbc.Drivers;
import com.github.rmannibucau.jpa.cli.parameter.ParameterHolder;
import com.github.rmannibucau.jpa.cli.provider.DataSourceProvider;
import com.github.rmannibucau.jpa.cli.provider.EntityManagerProvider;
import jline.console.ConsoleReader;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.environments.Environment;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static java.util.Arrays.asList;

public class Cli {
    private final EntityManagerProvider emfProvider = new EntityManagerProvider();
    private final DataSourceProvider dataSourceProvider = new DataSourceProvider();
    private final PersistenceUnitInfoImpl info = new PersistenceUnitInfoImpl();
    private final Main crest = new Main(asList(
            DataSourceCommands.class, InfoCommands.class, JPACommands.class, GlobalCommands.class, ParameterCommands.class, ConfigurationCommands.class));
    private final Environment env = new CliEnvironment(this);
    private final ParameterHolder parameters = new ParameterHolder();
    private final Configuration configuration = new Configuration();
    private final AliasesManager aliases = new AliasesManager();

    private EntityManagerFactory emf;
    private EntityManager em;

    public Cli() {
        Drivers.load();
    }

    private void parse(final String[] args) {
        try {
            crest.main(env, args);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(final String line) {
        parse(toArgs(aliases.findOrNoop(line)));
    }

    public PersistenceUnitInfoImpl getInfo() {
        return info;
    }

    public DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

    public ParameterHolder getParameters() {
        return parameters;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public AliasesManager getAliases() {
        return aliases;
    }

    public EntityManager em(final boolean create) {
        if (create) {
            if (emf == null) {
                emf = emfProvider.newFactory(info);
            }
            if (em == null) {
                em = emf.createEntityManager();
            }
        }
        return em;
    }

    public void reset() {
        if (em != null) {
            em.close();
            em = null;
        }
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }

    public static void main(final String[] args) throws IOException {
        final Cli cli = new Cli();
        if (args != null && args.length > 0) {
            cli.parse(args);
        }

        final ConsoleReader reader = new ConsoleReader();
        String line;
        final String prompt = System.getProperty("user.name", "jpacli") + " > ";
        while ((line = reader.readLine(prompt)) != null) {
            if (asList("quit", "q", "exit", "x").contains(line.trim())) {
                break;
            }
            cli.execute(line);
        }
    }

    private static String[] toArgs(final String raw) {
        final Collection<String> result = new LinkedList<>();

        Character end = null;
        boolean escaped = false;
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);
            if (escaped) {
                escaped = false;
                current.append(c);
            } else if ((end != null && end == c) || (c == ' ' && end == null)) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                end = null;
            } else if (c == '\\') {
                escaped = true;
            } else if (end == null && (c == '"' || c == '\'')) {
                end = c;
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result.toArray(new String[result.size()]);
    }
}
