package com.daeliin.components.domain.pagination;

import com.google.common.base.MoreObjects;

import java.util.Objects;

public final class Sort implements Comparable<Sort> {

    public enum Direction {
        ASC, DESC
    }

    public final String property;
    public final Direction direction;

    public Sort(String property) {
        this(property, Direction.ASC);
    }

    public Sort(String property, Direction direction) {
        this.property = Objects.requireNonNull(property, "property should not be null");
        this.direction = direction != null ? direction : Direction.ASC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sort sort = (Sort) o;
        return Objects.equals(property, sort.property) &&
                direction == sort.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, direction);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("property", property)
                .add("direction", direction)
                .toString();
    }

    @Override
    public int compareTo(Sort other) {
        if (this.equals(other)) {
            return 0;
        }

        return property.compareTo(other.property);
    }
}