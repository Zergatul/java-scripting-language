package com.zergatul.scripting.compiler;

import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.*;

final class TypeHierarchy {

    private static final String OBJECT = "java/lang/Object";

    private final @Nullable ClassLoader classLoader;
    private final Map<String, TypeInfo> types = new HashMap<>();

    public TypeHierarchy(@Nullable ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void register(String name, boolean isInterface, @Nullable String superName, @Nullable String[] interfaces) {
        types.put(name, new TypeInfo(isInterface, superName, interfaces == null ? List.of() : List.of(interfaces)));
    }

    public String getCommonSuperClass(String type1, String type2) {
        TypeInfo info1 = get(type1);
        TypeInfo info2 = get(type2);
        if (isAssignableFrom(type1, type2)) {
            return type1;
        }
        if (isAssignableFrom(type2, type1)) {
            return type2;
        }
        if (info1.isInterface || info2.isInterface) {
            return OBJECT;
        }

        // Match ASM's superclass selection, using metadata instead of loading generated classes.
        do {
            type1 = get(type1).superName;
        } while (!isAssignableFrom(type1, type2));
        return type1;
    }

    private boolean isAssignableFrom(String target, String source) {
        if (target.equals(OBJECT)) {
            return true;
        }

        Set<String> visited = new HashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.add(source);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (target.equals(current)) {
                return true;
            }
            if (!visited.add(current)) {
                continue;
            }

            TypeInfo info = get(current);
            if (info.superName != null) {
                pending.add(info.superName);
            }
            pending.addAll(info.interfaces);
        }
        return false;
    }

    private TypeInfo get(String name) {
        TypeInfo info = types.get(name);
        if (info != null) {
            return info;
        }

        Class<?> type;
        try {
            type = Class.forName(name.replace('/', '.'), false, classLoader);
        } catch (ClassNotFoundException e) {
            // A Java interop loader need not expose the compiler's own runtime classes.
            try {
                type = Class.forName(name.replace('/', '.'), false, TypeHierarchy.class.getClassLoader());
            } catch (ClassNotFoundException cause) {
                throw new TypeNotPresentException(name, cause);
            }
        }

        Class<?> superclass = type.getSuperclass();
        info = new TypeInfo(
                type.isInterface(),
                superclass == null ? null : Type.getInternalName(superclass),
                Arrays.stream(type.getInterfaces()).map(Type::getInternalName).toList());
        types.put(name, info);
        return info;
    }

    private record TypeInfo(boolean isInterface, @Nullable String superName, List<String> interfaces) {}
}