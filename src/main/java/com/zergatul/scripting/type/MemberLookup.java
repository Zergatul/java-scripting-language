package com.zergatul.scripting.type;

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

    private record MethodKey(String name, List<SType> parameters, boolean isStatic) {

        private MethodKey(MethodReference method) {
            this(
                    method.getName(),
                    method instanceof NativeMethodReference nativeMethod
                            ? Arrays.stream(nativeMethod.getUnderlying().getParameterTypes())
                                    .map(SType::fromJavaType)
                                    .toList()
                            : method.getParameterTypes(),
                    method.isStatic());
        }

        @Override
        public int hashCode() {
            // Some SType implementations have semantic equals without a matching hashCode.
            // Keep the hash coarse and let List.equals compare the parameter types.
            return Objects.hash(name, parameters.size(), isStatic);
        }
    }

    private record PropertyKey(String name, boolean isStatic) {

        private PropertyKey(PropertyReference property) {
            this(property.getName(), property.isStatic());
        }
    }
}