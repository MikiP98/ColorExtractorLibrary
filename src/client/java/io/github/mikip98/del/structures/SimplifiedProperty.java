package io.github.mikip98.del.structures;

import java.util.Set;

public class SimplifiedProperty {
    public String name;
    @SuppressWarnings("rawtypes")
    public Set<Comparable> allowedValues;

    @SuppressWarnings("rawtypes")
    public SimplifiedProperty(String name, Set<Comparable> allowedValues) {
        this.name = name;
        this.allowedValues = allowedValues;
    }
}
