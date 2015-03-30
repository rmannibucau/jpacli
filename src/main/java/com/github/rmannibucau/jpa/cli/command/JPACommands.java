package com.github.rmannibucau.jpa.cli.command;

import com.github.rmannibucau.jpa.cli.Cli;
import com.github.rmannibucau.jpa.cli.console.Lines;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import static com.github.rmannibucau.jpa.cli.command.Commands.cli;

@Command("jpa")
public class JPACommands {
    @Command("query")
    public void query(@Option({ "query", "q" }) final String query,
                      @Option({ "transaction", "tx", "t" }) @Default("false") final boolean transaction,
                      @Option({ "commit", "ci" }) @Default("false") final boolean commit,
                      @Option("collection") @Default("false") final boolean collection,
                      @Option("relationship") @Default("false") final boolean relationship,
                      @Option("start") @Default("-1") final int start,
                      @Option("max") @Default("100") final int max) {
        final Cli cli = cli();
        if (cli.getInfo().getJtaDataSource() == null && cli.getDataSourceProvider().hasDataSources()) {
            cli.getInfo().setDataSource(cli.getDataSourceProvider().first());
        }

        final EntityManager em = cli.em(true);
        if (transaction) {
            em.getTransaction().begin();
        }

        final Query q = em.createQuery(query);
        if (start >= 0) {
            q.setFirstResult(start);
        }
        if (max >= 0) {
            q.setMaxResults(max);
        }
        final List<?> results = q.getResultList();
        if (results == null || results.isEmpty()) {
            System.out.println("No result");
        } else {
            final EntityType<?> entityModel = em.getMetamodel().entity(results.iterator().next().getClass());
            if (entityModel == null) {
                for (final Object o : results) {
                    System.out.println(String.valueOf(o));
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
                final Lines lines = new Lines(columns);
                for (final Object o : results) {
                    final Collection<String> values = new LinkedList<>();
                    for (final String s : columns) {
                        final Member javaMember = fields.get(s).getJavaMember();
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
                    lines.line(values);
                }
                lines.printWithoutIntermidateHorizontalDelimiter(System.out);
                System.out.println();
            }
        }

        if (transaction) {
            if (commit) {
                em.getTransaction().commit();
            } else {
                em.getTransaction().rollback();
            }
        }
        em.clear();
    }
}
