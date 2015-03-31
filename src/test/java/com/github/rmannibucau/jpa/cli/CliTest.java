package com.github.rmannibucau.jpa.cli;

import org.junit.BeforeClass;
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

    @BeforeClass
    public static void db() throws SQLException {
        createDb();
    }

    @Test
    public void selectAll() {
        final Cli cli = newCli();

        cli.execute("jpa query --query='select t from CliTest$ETest t'");
        assertTrue(stdout.getLog().contains(
                "===============================================" + lineSeparator() +
                        "| id |           timestamp           | value  |" + lineSeparator() +
                        "===============================================" + lineSeparator() +
                        "| 1  | Mon Mar 30 00:00:00 CEST 2015 | test#1 |" + lineSeparator() +
                        "| 2  | Mon Mar 30 00:00:00 CEST 2015 | test#2 |" + lineSeparator() +
                        "-----------------------------------------------" + lineSeparator()));
    }

    @Test
    public void vertical() {
        final Cli cli = newCli();
        cli.execute("jpa query --query='select t from CliTest$ETest t' --vertical");
        assertTrue(stdout.getLog().contains(
                "       id\t1" + lineSeparator() +
                "timestamp\tMon Mar 30 00:00:00 CEST 2015" + lineSeparator() +
                "    value\ttest#1" + lineSeparator() +
                "" + lineSeparator() +
                "       id\t2" + lineSeparator() +
                "timestamp\tMon Mar 30 00:00:00 CEST 2015" + lineSeparator() +
                "    value\ttest#2"));
    }

    @Test
    public void verticalGlobal() {
        final Cli cli = newCli();
        cli.execute("setup table --format=VERTICAL");
        cli.execute("jpa query --query='select t from CliTest$ETest t'");
        assertTrue(stdout.getLog().contains(
                "       id\t1" + lineSeparator() +
                        "timestamp\tMon Mar 30 00:00:00 CEST 2015" + lineSeparator() +
                        "    value\ttest#1" + lineSeparator() +
                        "" + lineSeparator() +
                        "       id\t2" + lineSeparator() +
                        "timestamp\tMon Mar 30 00:00:00 CEST 2015" + lineSeparator() +
                        "    value\ttest#2"));
        cli.execute("setup table --format=HORIZONTAL"); // reset
    }

    @Test
    public void pagiantion() {
        final Cli cli = newCli();
        cli.execute("jpa query --query='select t from CliTest$ETest t' --max=1 --start=0");
        assertTrue(stdout.getLog().contains(
                "===============================================" + lineSeparator() +
                        "| id |           timestamp           | value  |" + lineSeparator() +
                        "===============================================" + lineSeparator() +
                        "| 1  | Mon Mar 30 00:00:00 CEST 2015 | test#1 |" + lineSeparator() +
                        "-----------------------------------------------" + lineSeparator()));
    }

    @Test
    public void aggregation() {
        final Cli cli = newCli();
        cli.execute("jpa query --query='select count(t) from CliTest$ETest t'");
        assertTrue(stdout.getLog().contains("2"));
    }

    @Test
    public void nativeQuery() {
        final Cli cli = newCli();
        cli.execute("jpa native-query --query='select id, value, timestamp from ETEST'");
        assertTrue(stdout.getLog().contains(
                "===========================" + lineSeparator() +
                "| ? |   ?    |     ?      |" + lineSeparator() +
                "===========================" + lineSeparator() +
                "| 1 | test#1 | 2015-03-30 |" + lineSeparator() +
                "| 2 | test#2 | 2015-03-30 |" + lineSeparator() +
                "---------------------------" + lineSeparator()));
    }

    @Test
    public void meta() {
        final Cli cli = newCli();
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
    }

    @Test
    public void parameters() {
        final Cli cli = newCli();
        cli.execute("parameter add --name=id --value=2 --type=long");
        cli.execute("jpa query --query='select t from CliTest$ETest t where t.id = :id'");
        assertTrue(stdout.getLog().contains("2  | Mon Mar 30 00:00:00 CEST 2015 | test#2"));
        assertFalse(stdout.getLog().contains("1  | Mon Mar 30 00:00:00 CEST 2015 | test#1"));
        stdout.clear();
    }

    @Test
    public void alias() {
        final Cli cli = newCli();
        cli.execute("setup set-alias --alias=theone --cmd='jpa query --query=\"select t from CliTest$ETest t\"'");
        cli.execute("theone");
        assertTrue(stdout.getLog().contains("" +
                "===============================================" + lineSeparator() +
                "| id |           timestamp           | value  |" + lineSeparator() +
                "===============================================" + lineSeparator() +
                "| 1  | Mon Mar 30 00:00:00 CEST 2015 | test#1 |" + lineSeparator() +
                "| 2  | Mon Mar 30 00:00:00 CEST 2015 | test#2 |" + lineSeparator() +
                "-----------------------------------------------" + lineSeparator()));
        stdout.clear();
    }

    private Cli newCli() {
        final Cli cli = new Cli();

        // test cli
        cli.execute("datasource register --url=jdbc:h2:mem:test --user=sa");
        cli.execute("info entity --class=com.github.rmannibucau.jpa.cli.CliTest$ETest");
        return cli;
    }

    private static void createDb() throws SQLException {
        try (final Connection c = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")) {
            for (final String statement : asList(
                    "create table if not exists ETEST (id integer not null, value char(10), timestamp date, primary key(id))",
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
