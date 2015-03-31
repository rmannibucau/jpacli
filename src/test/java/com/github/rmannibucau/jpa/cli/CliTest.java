package com.github.rmannibucau.jpa.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.StandardOutputStreamLog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CliTest {
    @Rule
    public final StandardOutputStreamLog stdout = new StandardOutputStreamLog();

    @Test
    public void query() throws SQLException {
        createDb();

        final Cli cli = new Cli();

        // test cli
        cli.execute("datasource register --url=jdbc:h2:mem:test --user=sa");
        cli.execute("info entity --class=com.github.rmannibucau.jpa.cli.CliTest$ETest");

        // default
        cli.execute("jpa query --query='select t from CliTest$ETest t'");
        assertTrue(stdout.getLog().contains(
                "===============================================" + lineSeparator() +
                        "| id |           timestamp           | value  |" + lineSeparator() +
                        "===============================================" + lineSeparator() +
                        "| 1  | Mon Mar 30 00:00:00 CEST 2015 | test#1 |" + lineSeparator() +
                        "| 2  | Mon Mar 30 00:00:00 CEST 2015 | test#2 |" + lineSeparator() +
                        "-----------------------------------------------" + lineSeparator()));
        stdout.clear();

        // pagination
        cli.execute("jpa query --query='select t from CliTest$ETest t' --max=1 --start=0");
        assertTrue(stdout.getLog().contains(
                "===============================================" + lineSeparator() +
                "| id |           timestamp           | value  |" + lineSeparator() +
                "===============================================" + lineSeparator() +
                "| 1  | Mon Mar 30 00:00:00 CEST 2015 | test#1 |" + lineSeparator() +
                "-----------------------------------------------" + lineSeparator()));
        stdout.clear();

        // aggregation
        cli.execute("jpa query --query='select count(t) from CliTest$ETest t'");
        assertTrue(stdout.getLog().contains("2"));
        stdout.clear();

        // native
        cli.execute("jpa native-query --query='select id, value, timestamp from ETEST'");
        assertTrue(stdout.getLog().contains(
                "===========================" + lineSeparator() +
                "| ? |   ?    |     ?      |" + lineSeparator() +
                "===========================" + lineSeparator() +
                "| 1 | test#1 | 2015-03-30 |" + lineSeparator() +
                "| 2 | test#2 | 2015-03-30 |" + lineSeparator() +
                "---------------------------" + lineSeparator()));
        stdout.clear();

        // meta
        cli.execute("jpa meta");
        assertTrue(stdout.getLog().contains(
                "com.github.rmannibucau.jpa.cli.CliTest$ETest (ENTITY)" + lineSeparator() +
                "=================================" + lineSeparator() +
                "|   name    |  type  | category |" + lineSeparator() +
                "=================================" + lineSeparator() +
                "|    id     |  long  |  BASIC   |" + lineSeparator() +
                "| timestamp |  Date  |  BASIC   |" + lineSeparator() +
                "|   value   | String |  BASIC   |" + lineSeparator() +
                "---------------------------------"));
        stdout.clear();

        cli.execute("jpa meta --type=" + ETest.class.getName());
        assertTrue(stdout.getLog().contains(
                "com.github.rmannibucau.jpa.cli.CliTest$ETest (ENTITY)" + lineSeparator() +
                "=================================" + lineSeparator() +
                "|   name    |  type  | category |" + lineSeparator() +
                "=================================" + lineSeparator() +
                "|    id     |  long  |  BASIC   |" + lineSeparator() +
                "| timestamp |  Date  |  BASIC   |" + lineSeparator() +
                "|   value   | String |  BASIC   |" + lineSeparator() +
                "---------------------------------"));
        stdout.clear();

        // parameters
        cli.execute("parameter add --name=id --value=2 --type=long");
        cli.execute("jpa query --query='select t from CliTest$ETest t where t.id = :id'");
        assertTrue(stdout.getLog().contains("2  | Mon Mar 30 00:00:00 CEST 2015 | test#2"));
        assertFalse(stdout.getLog().contains("1  | Mon Mar 30 00:00:00 CEST 2015 | test#1"));
        stdout.clear();
    }

    private void createDb() throws SQLException {
        try (final Connection c = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")) {
            for (final String statement : asList(
                    "create table ETEST (id integer not null, value char(10), timestamp date, primary key(id))",
                    "insert into ETEST (id, value, timestamp) values(1, 'test#1', {ts '2015-03-30 23:25:52.69'})",
                    "insert into ETEST (id, value, timestamp) values(2, 'test#2', {ts '2015-03-30 23:25:52.69'})"
            )) {
                try (final Statement s = c.createStatement()) {
                    s.executeUpdate(statement);
                }
            }
            c.commit();
        }
        // check out create/insert statements
        try (final Connection c = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")) {
            try (final PreparedStatement preparedStatement = c.prepareStatement("SELECT t0.id, t0.timestamp, t0.value FROM PUBLIC.ETEST t0")) {
                final ResultSet rs = preparedStatement.executeQuery();
                assertTrue(rs.next());
                assertTrue(rs.next());
            }
            c.rollback();
        }
    }

    @Entity
    @Table(name = "ETEST")
    public static class ETest {
        @Id
        private long id;
        private String value;
        private Date timestamp;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }
}
