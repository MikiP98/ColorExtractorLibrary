package io.github.mikip98.del.structures;

import java.util.Set;

public class QuantumProperty extends EProperty {
    public Set<SimplifiedProperty> possibleProperties;

    public QuantumProperty(String name, Set<SimplifiedProperty> possibleProperties) {
        super(name);
        this.possibleProperties = possibleProperties;
    }
}
