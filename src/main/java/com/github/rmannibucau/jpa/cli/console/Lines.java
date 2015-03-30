package com.github.rmannibucau.jpa.cli.console;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Lines {
    private final List<Line> lines = new LinkedList<>();
    private final String cr;
    private final Line header;

    public Lines(final Collection<String> header) {
        this.cr = System.lineSeparator();
        this.header = new Line(header.toArray(new String[header.size()]));
    }

    public Lines line(final Collection<String> columns) {
        if (!lines.isEmpty() && lines.iterator().next().getColumns().length != columns.size()) {
            throw new IllegalArgumentException("columns should have all the same size");
        }
        final Line line = new Line(columns.toArray(new String[columns.size()]));
        line.setCr(cr);
        lines.add(line);
        return this;
    }

    public void print(final PrintStream out) {
        print(out, true, true);
    }

    public void print(final PrintStream out, final boolean headers, final boolean horiDelim) {
        final Iterator<Line> it = lines.iterator();
        if (!it.hasNext()) {
            return;
        }

        final int[] max = max(lines, header);
        if (headers) {
            header.print(max, out, true, true);
        }
        it.next().print(max, out, horiDelim, false);
        while (it.hasNext()) {
            it.next().print(max, out, horiDelim, false);
        }
        if (!horiDelim) {
            for (final int aMax : max) {
                for (int j = 0; j < aMax; j++) {
                    out.write(Line.LINE_CHAR.charAt(0));
                }
            }
            for (int i = 0; i < max.length * 3 + 1; i++) {
                out.write(Line.LINE_CHAR.charAt(0));
            }
        }
    }

    public void printWithoutIntermidateHorizontalDelimiter(final PrintStream out) {
        print(out, true, false);
    }

    private static int[] max(final List<Line> lines, final Line header) {
        final int[] max = new int[lines.iterator().next().getColumns().length];
        for (final Line line : lines) {
            for (int i = 0; i < max.length; i++) {
                final int ll = line.getColumns()[i].length();
                if (max[i] == 0) { // init
                    max[i] = ll;
                } else if (max[i] < ll) {
                    max[i] = ll;
                }
            }
        }
        for (int i = 0; i < max.length; i++) {
            final int ll = header.getColumns()[i].length();
            if (max[i] == 0) { // init
                max[i] = ll;
            } else if (max[i] < ll) {
                max[i] = ll;
            }
        }
        return max;
    }

    public List<Line> getLines() {
        return lines;
    }

    public Lines sort() {
        Collections.sort(lines);
        return this;
    }
}
