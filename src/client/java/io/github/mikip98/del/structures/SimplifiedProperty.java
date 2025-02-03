package io.github.mikip98.del.structures;

import java.util.Set;
import java.util.function.Function;

public class SimplifiedProperty extends EProperty {
    @SuppressWarnings("rawtypes")
    public Set<Comparable> allowedValues;
    @SuppressWarnings("rawtypes")
    public Function<String, Comparable> converter;

    @SuppressWarnings("rawtypes")
    public SimplifiedProperty(String name, Set<Comparable> allowedValues, Function<String, Comparable> converter) {
        super(name);
        this.allowedValues = allowedValues;
        this.converter = converter;
    }

    @Override
    public String toString() {
        return "SimplifiedProperty{" + "name='" + name + '\'' + ", allowedValues=" + allowedValues + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != this.getClass()) return false;

        final SimplifiedProperty other = (SimplifiedProperty) obj;
        return this.name.equals(other.name)
                && this.allowedValues.equals(other.allowedValues)
                && (this.converter == other.converter || (this.converter != null && this.converter.equals(other.converter)));  // Handle possible null
    }
}
