package com.github.rmannibucau.jpa.cli.parameter;

import com.github.rmannibucau.jpa.cli.impl.ParameterImpl;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.persistence.Parameter;

import static javax.xml.bind.DatatypeConverter.parseDateTime;

public class ParameterHolder {
    private final Collection<ParameterImpl<?>> parameters = new LinkedList<>();

    public void clear() {
        parameters.clear();
    }

    public void deregister(final String name, final int position) {
        final Iterator<ParameterImpl<?>> it = parameters.iterator();
        while (it.hasNext()) {
            final Parameter<?> next = it.next();
            if (name != null && name.equals(next.getName())) {
                it.remove();
                break;
            }
            if (next.getPosition() != null && position == next.getPosition()) {
                it.remove();
                break;
            }
        }
    }

    public void register(final String name, final int position, final String type, final String value, final String pattern) {
        Class<?> clazz = String.class;
        Object val = value;
        if (type != null) { // some aliases for type (faster to type)
            switch (type) {
                case "string":
                    break;
                case "Int":
                    clazz = Integer.class;
                    val = Integer.parseInt(value.trim());
                    break;
                case "Long":
                    clazz = Long.class;
                    val = Long.parseLong(value.trim());
                    break;
                case "int":
                    clazz = int.class;
                    val = Integer.parseInt(value.trim());
                    break;
                case "long":
                    clazz = long.class;
                    val = Long.parseLong(value.trim());
                    break;
                case "date":
                    clazz = Date.class;
                    try {
                        val = toDate(value, pattern);
                    } catch (final ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                    break;
                case "date-sql":
                    clazz = java.sql.Date.class;
                    try {
                        val = new java.sql.Date(toDate(value, pattern).getTime());
                    } catch (final ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                    break;
                case "timestamp":
                    clazz = Timestamp.class;
                    try {
                        val = new Timestamp(toDate(value, pattern).getTime());
                    } catch (final ParseException e) {
                        e.printStackTrace();
                        return;
                    }
                    break;
                default: {
                    try {
                        clazz = Thread.currentThread().getContextClassLoader().loadClass(type.trim());
                    } catch (final ClassNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
        parameters.add(new ParameterImpl(name, position < 0 ? null : position, clazz, val, value));
    }

    private Date toDate(String value, String pattern) throws ParseException {
        return pattern != null ? new SimpleDateFormat(pattern).parse(value.trim()) : parseDateTime(value.trim()).getTime();
    }

    public Collection<ParameterImpl<?>> getParameters() {
        return parameters;
    }
}
