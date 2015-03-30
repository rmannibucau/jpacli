package com.github.rmannibucau.jpa.cli.provider;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DataSourceProvider {
    private final Map<String, DataSource> datasources = new HashMap<>();

    public void register(final String name, final String url, final String user, final String password) {
        datasources.put(name, new DataSourceImpl(url, user, password));
    }

    public void deregister(final String name) {
        datasources.remove(name);
    }

    public DataSource first() {
        return datasources.values().iterator().next();
    }

    public DataSource getDataSource(final String datasource) {
        if (!datasources.containsKey(datasource)) {
            throw new IllegalArgumentException(datasource + " not registered");
        }
        return datasources.get(datasource);
    }

    public boolean hasDataSources() {
        return !datasources.isEmpty();
    }

    private static class DataSourceImpl implements DataSource {
        private final String url;
        private final String user;
        private final String password;

        public DataSourceImpl(final String url, final String user, final String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }

        public Connection getConnection() throws SQLException {
            return user != null ? DriverManager.getConnection(url, user, password) : DriverManager.getConnection(url);
        }

        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        public PrintWriter getLogWriter() throws SQLException {
            throw new SQLException();
        }

        public void setLogWriter(final PrintWriter out) throws SQLException {
            throw new SQLException();
        }

        public void setLoginTimeout(final int seconds) throws SQLException {
            throw new SQLException();
        }

        public int getLoginTimeout() throws SQLException {
            throw new SQLException();
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        public <T> T unwrap(final Class<T> iface) throws SQLException {
            throw new SQLException();
        }

        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            throw new SQLException();
        }
    }
}
