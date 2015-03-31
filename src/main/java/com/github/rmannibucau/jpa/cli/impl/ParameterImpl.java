package com.github.rmannibucau.jpa.cli.impl;

import javax.persistence.Parameter;

public class ParameterImpl<T> implements Parameter<T> {
    private final String name;
    private final Integer position;
    private final Class<T> type;
    private final T value;
    private final String rawValue;

    public ParameterImpl(final String name, final Integer position, final Class<T> type, final T value, final String rawValue) {
        this.name = name;
        this.position = position;
        this.type = type;
        this.value = value;
        this.rawValue = rawValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getPosition() {
        return position;
    }

    @Override
    public Class<T> getParameterType() {
        return type;
    }

    public String getRawValue() {
        return rawValue;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Parameter)) {
            return false;
        }
        final Parameter<?> that = Parameter.class.cast(other);
        if (name != null)
            return name.equals(that.getName());
        return position != null && position.equals(that.getPosition());
    }

    @Override
    public int hashCode() {
        return type.hashCode() ^ ((name != null) ? name.hashCode() : 0) ^ ((position != null) ? position.hashCode() : 0);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("Parameter");
        buf.append("<").append(getParameterType().getSimpleName()).append(">");
        if (name != null) {
            buf.append("('").append(name).append("')");
        } else if (position != null) {
            buf.append("(").append(position).append(")");
        } else {
            buf.append("(?)");
        }
        return buf.toString();
    }

    public T getValue() {
        return value;
    }
}
