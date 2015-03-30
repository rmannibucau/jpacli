package com.github.rmannibucau.jpa.cli.jdbc;

import static java.util.Arrays.asList;

public final class Drivers {
    static {
        // try to load well know drivers to automatically handle getConnection() from url
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (final String driver : asList(
                "org.h2.Driver",
                "org.hsqldb.jdbcDriver",
                "oracle.jdbc.OracleDriver",
                "com.mysql.jdbc.Driver",
                "org.postgresql.Driver")) {
            try {
                Class.forName(driver, true, loader);
            } catch (final ClassNotFoundException e) {
                // no-op
            }
        }
    }

    public static void load() {
        // no-op
    }

    private Drivers() {
        // no-op
    }
}
