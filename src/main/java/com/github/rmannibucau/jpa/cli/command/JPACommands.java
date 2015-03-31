package com.github.rmannibucau.jpa.cli.command;

import com.github.rmannibucau.jpa.cli.Cli;
import com.github.rmannibucau.jpa.cli.configuration.Configuration;
import com.github.rmannibucau.jpa.cli.console.Table;
import com.github.rmannibucau.jpa.cli.impl.ParameterImpl;
import com.github.rmannibucau.jpa.cli.parameter.ParameterHolder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.StreamingOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;
import static java.util.Arrays.asList;

@Command("jpa")
public class JPACommands {
    @Command("meta")
    public static StreamingOutput meta(@Option({"type", "t"}) final String type, @Option("vertical") final Boolean vertical) throws ClassNotFoundException {
        final Class<?> clazz = type == null ? null : Thread.currentThread().getContextClassLoader().loadClass(type);
        final EntityManager em = init(false);
        final Metamodel meta = em.getMetamodel();
        return new StreamingOutput() {
            @Override
            public void write(final OutputStream outputStream) throws IOException {
                final PrintStream ps = new PrintStream(outputStream);
                if (clazz != null) {
                    dumpType(ps, meta.entity(clazz), vertical);
                } else {
                    final List<ManagedType<?>> managedTypes = new ArrayList<>(meta.getManagedTypes());
                    Collections.sort(managedTypes, new Comparator<ManagedType<?>>() {
                        @Override
                        public int compare(final ManagedType<?> o1, final ManagedType<?> o2) {
                            return o1.getJavaType().getName().compareTo(o2.getJavaType().getName());
                        }
                    });
                    final Collection<String> seen = new HashSet<>();
                    for (final ManagedType<?> managedType : managedTypes) {
                        if (!seen.add(managedType.getJavaType().getName())) { // subclassing, prevent to print the same entity twice
                            continue;
                        }
                        dumpType(ps, managedType, vertical);
                    }
                }
            }
        };
    }

    @Command("query")
    public static StreamingOutput query(
            @Option({"query", "q"}) final String query,
            @Option({"transaction", "tx", "t"}) @Default("false") final boolean transaction,
            @Option({"commit", "ci"}) @Default("false") final boolean commit,
            @Option("collection") @Default("false") final boolean collection,
            @Option("relationship") @Default("false") final boolean relationship,
            @Option("vertical") final Boolean vertical,
            @Option("start") @Default("-1") final int start,
            @Option("max") @Default("100") final int max) {
        final EntityManager em = init(transaction);
        try {
            final Query q = em.createQuery(query);
            return execute(collection, relationship, start, max, vertical, em, q);
        } finally {
            release(transaction, commit, em);
        }
    }

    @Command("named-query")
    public static StreamingOutput namedQuery(
            @Option({"query", "q"}) final String query,
            @Option({"transaction", "tx", "t"}) @Default("false") final boolean transaction,
            @Option({"commit", "ci"}) @Default("false") final boolean commit,
            @Option("collection") @Default("false") final boolean collection,
            @Option("relationship") @Default("false") final boolean relationship,
            @Option("vertical") @Default("false") final boolean vertical,
            @Option("start") @Default("-1") final int start,
            @Option("max") @Default("100") final int max) {
        final EntityManager em = init(transaction);
        try {
            final Query q = em.createNamedQuery(query);
            return execute(collection, relationship, start, max, vertical, em, q);
        } finally {
            release(transaction, commit, em);
        }
    }

    @Command("native-query")
    public static StreamingOutput nativeQuery(
            @Option({"query", "q"}) final String query,
            @Option({"transaction", "tx", "t"}) @Default("false") final boolean transaction,
            @Option({"commit", "ci"}) @Default("false") final boolean commit,
            @Option("collection") @Default("false") final boolean collection,
            @Option("relationship") @Default("false") final boolean relationship,
            @Option("vertical") @Default("false") final boolean vertical,
            @Option("start") @Default("-1") final int start,
            @Option("max") @Default("100") final int max) {
        final EntityManager em = init(transaction);
        try {
            final Query q = em.createNativeQuery(query);
            return execute(collection, relationship, start, max, vertical, em, q);
        } finally {
            release(transaction, commit, em);
        }
    }

    private static StreamingOutput execute(final boolean collection, final boolean relationship,
                                           final int start, final int max,
                                           final Boolean vertical,
                                           final EntityManager em, final Query query) {
        if (start >= 0) {
            query.setFirstResult(start);
        }
        if (max >= 0) {
            query.setMaxResults(max);
        }
        final Set<Parameter<?>> parameters = query.getParameters();
        if (parameters != null) {
            final ParameterHolder cliParameters = cli().getParameters();
            for (final Parameter<?> parameter : parameters) {
                for (final ParameterImpl p : cliParameters.getParameters()) {
                    if ((p.getName() != null && parameter.getName() != null && p.getName().equals(parameter.getName()))
                            || (parameter.getPosition() != null && p.getPosition() != null && p.getPosition().equals(parameter.getPosition()))) {
                        query.setParameter(p, p.getValue());
                        break;
                    }
                }
            }
        }
        final List<?> results = query.getResultList();
        return new StreamingOutput() {
            @Override
            public void write(final OutputStream outputStream) throws IOException {
                final PrintStream ps = new PrintStream(outputStream);
                if (results == null || results.isEmpty()) {
                    ps.println("No result");
                } else {
                    final Object first = results.iterator().next();
                    final EntityType<?> entityModel = em.getMetamodel().entity(first.getClass());
                    if (entityModel == null) {
                        if (first.getClass().isArray()) {
                            final int length = Array.getLength(first);
                            final String[] headers = new String[length];
                            Arrays.fill(headers, "?");
                            final Table table = new Table(asList(headers));
                            for (final Object o : results) {
                                final Collection<String> line = new LinkedList<>();
                                for (int i = 0; i < length; i++) {
                                    line.add(String.valueOf(Array.get(o, i)));
                                }
                                table.line(line);
                            }
                            if (isVertical(vertical)) {
                                table.printVertically(ps);
                            } else {
                                table.printHorizontally(ps);
                            }
                        } else {
                            for (final Object o : results) {
                                ps.println(String.valueOf(o));
                            }
                        }
                    } else {
                        final Map<String, Attribute<?, ?>> fields = new TreeMap<>();
                        for (final Attribute<?, ?> attr : entityModel.getAttributes()) {
                            if ((!relationship && attr.isAssociation()) || (!collection && attr.isCollection())) {
                                continue;
                            }
                            fields.put(attr.getName(), attr);
                            final Member javaMember = attr.getJavaMember();
                            if (AccessibleObject.class.isInstance(javaMember) && !AccessibleObject.class.cast(javaMember).isAccessible()) {
                                AccessibleObject.class.cast(javaMember).setAccessible(true);
                            }
                        }

                        final Set<String> columns = fields.keySet();
                        final Table table = new Table(columns);
                        for (final Object o : results) {
                            final Collection<String> values = new LinkedList<>();
                            for (final String s : columns) {
                                final Attribute<?, ?> attribute = fields.get(s);
                                final Member javaMember = attribute.getJavaMember();
                                try {
                                    if (Method.class.isInstance(javaMember)) {
                                        values.add(String.valueOf(Method.class.cast(javaMember).invoke(o)));
                                    } else if (Field.class.isInstance(javaMember)) {
                                        values.add(String.valueOf(Field.class.cast(javaMember).get(o)));
                                    }
                                } catch (final Exception e) {
                                    values.add("??" + e.getMessage() + "??");
                                }
                            }
                            table.line(values);
                        }
                        if (isVertical(vertical)) {
                            table.printVertically(ps);
                        } else {
                            table.printHorizontally(ps);
                        }
                    }
                }
            }
        };
    }

    private static boolean isVertical(final Boolean vertical) {
        return (vertical != null && vertical) || (vertical == null && cli().getConfiguration().getOutput() == Configuration.Output.VERTICAL);
    }

    private static EntityManager init(final boolean transaction) {
        final Cli cli = cli();
        if (cli.getInfo().getJtaDataSource() == null && cli.getDataSourceProvider().hasDataSources()) {
            cli.getInfo().setDataSource(cli.getDataSourceProvider().first());
        }

        final EntityManager em = cli.em(true);
        if (transaction) {
            em.getTransaction().begin();
        }
        return em;
    }

    private static void release(final boolean transaction, final boolean commit, final EntityManager em) {
        if (transaction) {
            if (commit) {
                em.getTransaction().commit();
            } else {
                em.getTransaction().rollback();
            }
        }
        em.clear();
    }

    private static void dumpType(final PrintStream ps, final ManagedType<?> entityType, final Boolean vertical) {
        ps.println(entityType.getJavaType().getName() + " (" + entityType.getPersistenceType() + ")");

        final List<Attribute<?, ?>> attributes = new ArrayList<Attribute<?, ?>>(entityType.getAttributes());
        Collections.sort(attributes, new Comparator<Attribute<?, ?>>() {
            @Override
            public int compare(final Attribute<?, ?> o1, final Attribute<?, ?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        final Table table = new Table(asList("name", "type", "category"));
        for (final Attribute<?, ?> attribute : attributes) {
            table.line(asList(attribute.getName(), attribute.getJavaType().getName().replace("java.lang.", "").replace("java.util.", ""), attribute.getPersistentAttributeType().name()));
        }
        if (isVertical(vertical)) {
            table.printVertically(ps);
        } else {
            table.printHorizontally(ps);
        }
        ps.println();
    }
}
