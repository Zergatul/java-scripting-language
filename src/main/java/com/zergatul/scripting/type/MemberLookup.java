package com.zergatul.scripting.type;

import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class MemberLookup {

    private MemberLookup() {}

    public static List<MethodReference> getMethods(SType type) {
        Map<MethodKey, MethodReference> methods = new LinkedHashMap<>();
        collectMethods(type, true, methods, new HashSet<>());
        return new ArrayList<>(methods.values());
    }

    public static List<PropertyReference> getProperties(SType type) {
        Map<PropertyKey, PropertyReference> properties = new LinkedHashMap<>();
        collectProperties(type, true, properties, new HashSet<>());
        return new ArrayList<>(properties.values());
    }

    private static void collectMethods(
            SType type,
            boolean root,
            Map<MethodKey, MethodReference> methods,
            Set<SType> visited
    ) {
        if (type == null || (!root && type == SJavaObject.instance) || !visited.add(type)) {
            return;
        }

        for (MethodReference method : type.getDeclaredMethods()) {
            if (root || (!method.isStatic() && method.getVisibility() != Visibility.PRIVATE)) {
                methods.putIfAbsent(new MethodKey(method), method);
            }
        }

        collectMethods(type.getBaseType(), false, methods, visited);
        for (SType interfaceType : type.getInterfaces()) {
            collectMethods(interfaceType, false, methods, visited);
        }
    }

    private static void collectProperties(
            SType type,
            boolean root,
            Map<PropertyKey, PropertyReference> properties,
            Set<SType> visited
    ) {
        if (type == null || (!root && type == SJavaObject.instance) || !visited.add(type)) {
            return;
        }

        for (PropertyReference property : type.getDeclaredProperties()) {
            if (root || (!property.isStatic() && property.getVisibility() != Visibility.PRIVATE)) {
                properties.putIfAbsent(new PropertyKey(property), property);
            }
        }

        collectProperties(type.getBaseType(), false, properties, visited);
        for (SType interfaceType : type.getInterfaces()) {
            collectProperties(interfaceType, false, properties, visited);
        }
    }

    private static final class MethodKey {

        private final String name;
        private final List<SType> parameters;
        private final boolean isStatic;

        private MethodKey(String name, List<SType> parameters, boolean isStatic) {
            this.name = name;
            this.parameters = parameters;
            this.isStatic = isStatic;
        }

        private MethodKey(MethodReference method) {
            this(
                    method.getName(),
                    method instanceof NativeMethodReference
                            ? Lists.from(
                                    Arrays.stream(((NativeMethodReference) method).getUnderlying().getParameterTypes())
                                    .map(SType::fromJavaType))
                            : method.getParameterTypes(),
                    method.isStatic());
        }

        @Override
        public int hashCode() {
            // Some SType implementations have semantic equals without a matching hashCode.
            // Keep the hash coarse and let List.equals compare the parameter types.
            return Objects.hash(name, parameters.size(), isStatic);
        }

        public String name() {
            return name;
        }

        public List<SType> parameters() {
            return parameters;
        }

        public boolean isStatic() {
            return isStatic;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            MethodKey that = (MethodKey) obj;
            return  Objects.equals(this.name, that.name) &&
                    Objects.equals(this.parameters, that.parameters) &&
                    this.isStatic == that.isStatic;
        }

        @Override
        public String toString() {
            return  "MethodKey[" +
                    "name=" + name + ", " +
                    "parameters=" + parameters + ", " +
                    "isStatic=" + isStatic + ']';
        }
    }

    private static final class PropertyKey {

        private final String name;
        private final boolean isStatic;

        private PropertyKey(String name, boolean isStatic) {
            this.name = name;
            this.isStatic = isStatic;
        }

        private PropertyKey(PropertyReference property) {
            this(property.getName(), property.isStatic());
        }

        public String name() {
            return name;
        }

        public boolean isStatic() {
            return isStatic;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            PropertyKey that = (PropertyKey) obj;
            return  Objects.equals(this.name, that.name) &&
                    this.isStatic == that.isStatic;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, isStatic);
        }

        @Override
        public String toString() {
            return  "PropertyKey[" +
                    "name=" + name + ", " +
                    "isStatic=" + isStatic + ']';
        }
    }
}