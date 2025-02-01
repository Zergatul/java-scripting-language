package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.type.PropertyReference;

public class PropertySuggestion extends Suggestion {

    private final PropertyReference property;

    public PropertySuggestion(PropertyReference property) {
        this.property = property;
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