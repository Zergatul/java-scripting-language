package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.tests.completion.helpers.SuggestionHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.type.PropertyReference;
import com.zergatul.scripting.type.MemberLookup;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.Visibility;
import org.junit.jupiter.api.Assertions;

import java.util.Optional;

public class PropertySuggestion extends Suggestion {

    private final PropertyReference property;

    public PropertySuggestion(PropertyReference property) {
        this.property = property;
    }

    public static PropertySuggestion getInstance(Class<?> clazz, String name) {
        PropertyReference property = MemberLookup.getProperties(SType.fromJavaType(clazz)).stream()
                .filter(p -> !p.isStatic())
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow();
        return new PropertySuggestion(property);
    }

    public static PropertySuggestion getStatic(Class<?> clazz, String name) {
        PropertyReference property = MemberLookup.getProperties(SType.fromJavaType(clazz)).stream()
                .filter(PropertyReference::isStatic)
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow();
        return new PropertySuggestion(property);
    }

    public static PropertySuggestion getInstance(TestCompletionContext context, String className, String propertyName) {
        PropertyReference property = MemberLookup.getProperties(SuggestionHelper.extractClassType(context, className)).stream()
                .filter(p -> !p.isStatic())
                .filter(p -> p.getName().equals(propertyName))
                .findFirst()
                .orElseThrow();
        return new PropertySuggestion(property);
    }

    public static PropertySuggestion getStatic(SType type, String name) {
        Optional<PropertyReference> optional = MemberLookup.getProperties(type).stream()
                .filter(PropertyReference::isStatic)
                .filter(r -> r.getName().equals(name))
                .findFirst();
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

    @Override
    public String toString() {
        return String.format("%s %s %s", property.getVisibility() == Visibility.PUBLIC ? "public" : "private", property.getType(), property.getName());
    }
}