package com.github.rmannibucau.jpa.cli.console;

import java.io.PrintStream;

public class Line implements Comparable<Line> {
    public static final String COL_SEP = "|";
    public static final String HEADER_CHAR = "=";
    public static final String LINE_CHAR = "-";
    public static final char EMPTY_CHAR = ' ';

    private final String[] columns;
    private String cr = System.lineSeparator();

    public Line(final String... columns) {
        this.columns = columns;
    }

    public String[] getColumns() {
        return columns;
    }

    public void print(final int[] max, final PrintStream out, final boolean horizontalDelimiter, final boolean header) {
        final StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < max.length; i++) {
            sb.append(EMPTY_CHAR);
            final int spaces = max[i] - columns[i].length();
            for (int k = 0; k < spaces / 2; k++) {
                sb.append(EMPTY_CHAR);
            }
            sb.append(columns[i]);
            for (int k = 0; k < spaces - spaces / 2; k++) {
                sb.append(EMPTY_CHAR);
            }
            sb.append(EMPTY_CHAR).append(COL_SEP);
        }

        final String lineStr = sb.toString();

        final StringBuilder sep = new StringBuilder(header ? HEADER_CHAR : "|");
        final String s;
        if (header) {
            s = HEADER_CHAR;
        } else {
            s = LINE_CHAR;
        }
        for (int i = 0; i < lineStr.length() - 1; i++) {
            sep.append(s);
        }

        if (header) {
            printLine(out, sep.toString());
        }

        printLine(out, lineStr);
        if (horizontalDelimiter) {
            printLine(out, sep.toString());
        }
    }

    private void printLine(final PrintStream out, final String s) {
        out.print(s + cr);
    }

    public void setCr(final String cr) {
        this.cr = cr;
    }

    @Override
    public int compareTo(final Line o) {
        for (int i = 0; i < columns.length; i++) {
            int cmp = String.valueOf(columns[i]).compareTo(String.valueOf(o.columns[i]));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
