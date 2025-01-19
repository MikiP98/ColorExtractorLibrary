package io.github.mikip98.del.extractors;

import io.github.mikip98.del.structures.SimplifiedProperty;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
                        ) throw new RuntimeException("Duplicate property name: " + property.getName());

                        propertyName2SimplifiedPropertyMap.put(
                            property.getName(), new SimplifiedProperty(
                                    property.getName(),
                                    values
                            )
                        );
                    }
            );
        }

        return propertyName2SimplifiedPropertyMap;
    }
}
