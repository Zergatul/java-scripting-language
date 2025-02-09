package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.runtime.AsyncStateMachineException;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class PropertySuggestion extends Suggestion {

    private final PropertyReference property;

    public PropertySuggestion(PropertyReference property) {
        this.property = property;
    }

    public static PropertySuggestion getInstance() {
        throw new AssertionError();
    }

    public static PropertySuggestion getStatic(SType type, String name) {
        Optional<PropertyReference> optional = type.getStaticProperties().stream().filter(r -> r.getName().equals(name)).findFirst();
        if (optional.isEmpty()) {
            Assertions.fail();
            throw new AssertionError();
        } else {
            return new PropertySuggestion(optional.get());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertySuggestion other) {
            return other.property.equals(property);
        } else {
            return false;
        }
    }
}