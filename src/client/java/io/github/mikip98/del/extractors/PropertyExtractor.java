package io.github.mikip98.del.extractors;

import io.github.mikip98.del.structures.EProperty;
import io.github.mikip98.del.structures.QuantumProperty;
import io.github.mikip98.del.structures.SimplifiedProperty;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PropertyExtractor {

    @SuppressWarnings("rawtypes")
    public static @NotNull Map<String, EProperty> getPropertyName2EPropertyMap() {
        Map<String, EProperty> propertyName2EPropertyMap = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            block.getDefaultState().getProperties().forEach(
                    property -> {

                        HashSet<Comparable> values = new HashSet<>(property.getValues());
                        SimplifiedProperty newProperty = new SimplifiedProperty(
                                property.getName(),
                                values,
                                getParserForComparable(values.iterator().next())
                        );

                        if (!propertyName2EPropertyMap.containsKey(property.getName())) {
                            propertyName2EPropertyMap.put(property.getName(), newProperty);
                            return;
                        }

                        if (propertyName2EPropertyMap.get(property.getName()) instanceof SimplifiedProperty oldProperty) {
                            if (oldProperty.allowedValues.equals(values)) return;

                            Set<SimplifiedProperty> possibleProperties = new HashSet<>(Set.of(oldProperty, newProperty));
                            QuantumProperty newQuantumProperty = new QuantumProperty(property.getName(), possibleProperties);
                            propertyName2EPropertyMap.put(property.getName(), newQuantumProperty);
                        }
                        else if (propertyName2EPropertyMap.get(property.getName()) instanceof QuantumProperty oldProperty) {
                            if (oldProperty.possibleProperties.contains(newProperty)) return;
                            oldProperty.possibleProperties.add(newProperty);
                        }
                    }
            );
        }

        return propertyName2EPropertyMap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected static Function<String, Comparable> getParserForComparable(Comparable c) {
        if (c instanceof Enum) return (String s) -> Enum.valueOf((Class) c.getClass(), s.toUpperCase());

        else if (c instanceof Boolean) return Boolean::parseBoolean;
        else if (c instanceof Double) return Double::parseDouble;
        else if (c instanceof Float) return Float::parseFloat;
        else if (c instanceof Integer) return Integer::parseInt;
        else if (c instanceof Long) return Long::parseLong;
        else if (c instanceof String) return (String s) -> s;
        else return null;
    }
}
