package io.github.mikip98.del.extractors;

import io.github.mikip98.del.structures.SimplifiedProperty;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

public class PropertyExtractor {

    @SuppressWarnings("rawtypes")
    public static @NotNull Map<String, SimplifiedProperty> getPropertyName2SimplifiedPropertyMap() {
        Map<String, SimplifiedProperty> propertyName2SimplifiedPropertyMap = new HashMap<>();

        for (Block block : Registries.BLOCK) {
            block.getDefaultState().getProperties().forEach(
                    property -> {
                        HashSet<Comparable> values = new HashSet<>(property.getValues());

                        if (
                                propertyName2SimplifiedPropertyMap.containsKey(property.getName())
                                        && !propertyName2SimplifiedPropertyMap.get(property.getName()).allowedValues.equals(values)
                        ) {
//                            throw new RuntimeException(
//                                    "Duplicate property name: " + property.getName() + " with different values, original values: " +
//                                            propertyName2SimplifiedPropertyMap.get(property.getName()).allowedValues + ", new values: " + values
//                            );
                            propertyName2SimplifiedPropertyMap.get(property.getName()).allowedValues.addAll(values);
                            // TODO: Merge allowed values if possible (a.k.a. are of the same type), else ignore the new property :(

                        } else {
                            propertyName2SimplifiedPropertyMap.put(
                                    property.getName(), new SimplifiedProperty(
                                            property.getName(),
                                            values,
                                            getParserForComparable(values.iterator().next())
                                    )
                            );
                        }
                    }
            );
        }

        return propertyName2SimplifiedPropertyMap;
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
