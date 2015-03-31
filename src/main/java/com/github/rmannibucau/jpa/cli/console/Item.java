package com.github.rmannibucau.jpa.cli.console;

public class Item implements Comparable<Item> {
    private final String[] columns;

    public Item(final String... columns) {
        this.columns = columns;
    }

    public String[] getColumns() {
        return columns;
    }

    @Override
    public int compareTo(final Item o) {
        for (int i = 0; i < columns.length; i++) {
            int cmp = String.valueOf(columns[i]).compareTo(String.valueOf(o.columns[i]));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
