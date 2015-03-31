package com.github.rmannibucau.jpa.cli.console;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Table {
    private static final String COL_SEP = "|";
    private static final String HEADER_CHAR = "=";
    private static final char EMPTY_CHAR = ' ';
    private static final String LINE_CHAR = "-";

    private final List<Item> items = new LinkedList<>();
    private final String cr;
    private final Item header;

    public Table(final Collection<String> header) {
        this.cr = System.lineSeparator();
        this.header = new Item(header.toArray(new String[header.size()]));
    }

    public Table line(final Collection<String> columns) {
        if (!items.isEmpty() && items.iterator().next().getColumns().length != columns.size()) {
            throw new IllegalArgumentException("columns should have all the same size");
        }
        items.add(new Item(columns.toArray(new String[columns.size()])));
        return this;
    }

    private void printItemHorizontally(final Item item, final int[] max, final PrintStream out, final boolean horizontalDelimiter, final boolean header) {
        final StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < max.length; i++) {
            sb.append(EMPTY_CHAR);
            final int spaces = max[i] - item.getColumns()[i].length();
            for (int k = 0; k < spaces / 2; k++) {
                sb.append(EMPTY_CHAR);
            }
            sb.append(item.getColumns()[i]);
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
            out.print(sep.toString() + cr);
        }

        out.print(lineStr + cr);
        if (horizontalDelimiter) {
            out.print(sep.toString() + cr);
        }
    }

    public void printHorizontally(final PrintStream out) {
        final Iterator<Item> it = items.iterator();
        if (!it.hasNext()) {
            return;
        }

        final int[] max = horizontalMax(items, header);
        printItemHorizontally(header, max, out, true, true);
        printItemHorizontally(it.next(), max, out, false, false);
        while (it.hasNext()) {
            printItemHorizontally(it.next(), max, out, false, false);
        }
        for (final int aMax : max) {
            for (int j = 0; j < aMax; j++) {
                out.write(LINE_CHAR.charAt(0));
            }
        }
        for (int i = 0; i < max.length * 3 + 1; i++) {
            out.write(LINE_CHAR.charAt(0));
        }
        out.println();
    }

    private static int[] horizontalMax(final List<Item> items, final Item header) {
        final int[] max = new int[items.iterator().next().getColumns().length];
        for (final Item item : items) {
            for (int i = 0; i < max.length; i++) {
                max[i] = Math.max(max[i], item.getColumns()[i].length());
            }
        }
        for (int i = 0; i < max.length; i++) {
            max[i] = Math.max(max[i], header.getColumns()[i].length());
        }
        return max;
    }

    public void printVertically(final PrintStream ps) {
        final Iterator<Item> it = items.iterator();
        if (!it.hasNext()) {
            return;
        }

        int headerWidth = 0;
        for (final String h : header.getColumns()) {
            headerWidth = Math.max(headerWidth, h.length());
        }

        final int length = header.getColumns().length;
        for (final Item item : items) {
            for (int i = 0; i < length; i++) {
                ps.println(String.format("%" + headerWidth + "s\t%s", header.getColumns()[i], item.getColumns()[i]));
            }
            ps.println();
        }
        ps.println();
    }

    public List<Item> getItems() {
        return items;
    }

    public Table sort() {
        Collections.sort(items);
        return this;
    }
}
