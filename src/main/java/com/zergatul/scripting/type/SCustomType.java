package com.zergatul.scripting.type;

import com.zergatul.scripting.*;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import com.zergatul.scripting.type.operation.ReferenceTypeOperations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class SCustomType extends SType {

    private final Class<?> clazz;
    private final CustomType annotation;
    private final Lazy<List<PropertyReference>> properties;
    private final Lazy<List<MethodReference>> instanceMethods;
    private final Lazy<List<MethodReference>> staticMethods;
    private final Lazy<List<IndexOperation>> indexes;

    public SCustomType(Class<?> clazz) {
        CustomType annotation = clazz.getAnnotation(CustomType.class);
        if (annotation == null) {
            throw new InternalException("Custom type should annotated with @CustomType.");
        }

        this.clazz = clazz;
        this.annotation = annotation;
        this.properties = new Lazy<>(this::loadProperties);
        this.instanceMethods = new Lazy<>(this::loadInstanceMethods);
        this.staticMethods = new Lazy<>(this::loadStaticMethods);
        this.indexes = new Lazy<>(this::loadIndexes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SCustomType other) {
            return other.clazz == clazz;
        } else {
            return false;
        }
    }

    @Override
    public Class<?> getJavaClass() {
        return this.clazz;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
    }

    @Override
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        if (other instanceof SCustomType) {
            return ReferenceTypeOperations.EQUALS.value();
        } else {
            return null;
        }
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        if (other instanceof SCustomType) {
            return ReferenceTypeOperations.NOT_EQUALS.value();
        } else {
            return null;
        }
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public int getReturnInst() {
        return ARETURN;
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return properties.value();
    }

    @Override
    public PropertyReference getInstanceProperty(String name) {
        return properties.value().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public List<MethodReference> getInstanceMethods() {
        return instanceMethods.value();
    }

    @Override
    public List<MethodReference> getStaticMethods() {
        return staticMethods.value();
    }

    @Override
    public List<SType> supportedIndexers() {
        return indexes.value().stream().map(o -> o.indexType).toList();
    }

    @Override
    public IndexOperation index(SType type) {
        return indexes.value().stream().filter(o -> o.indexType.equals(type)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return annotation.name();
    }

    private List<PropertyReference> loadProperties() {
        List<PropertyReference> list = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            list.add(new FieldPropertyReference(field));
        }

        Map<String, Method> getters = new HashMap<>();
        Map<String, Method> setters = new HashMap<>();
        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            Getter getter = method.getAnnotation(Getter.class);
            if (getter != null) {
                if (list.stream().anyMatch(p -> p.getName().equals(getter.name())) || getters.containsKey(getter.name())) {
                    throw new InternalException(String.format("Method %s has invalid @Getter.", method.getName()));
                }
                getters.put(getter.name(), method);
            }
            Setter setter = method.getAnnotation(Setter.class);
            if (setter != null) {
                if (list.stream().anyMatch(p -> p.getName().equals(setter.name())) || setters.containsKey(setter.name())) {
                    throw new InternalException(String.format("Method %s has invalid @Setter.", method.getName()));
                }
                setters.put(setter.name(), method);
            }
        }

        for (Map.Entry<String, Method> entry : getters.entrySet()) {
            String name = entry.getKey();
            Method getter = entry.getValue();
            Method setter = setters.get(name);
            validateProperty(getter, setter);
            list.add(new AnnotatedPropertyReference(name, SType.fromJavaType(getter.getReturnType()), getter, setter));
        }

        return list;
    }

    private List<MethodReference> loadInstanceMethods() {
        return Arrays.stream(this.clazz.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> !m.isAnnotationPresent(Getter.class))
                .filter(m -> !m.isAnnotationPresent(Setter.class))
                .filter(m -> !m.isAnnotationPresent(IndexGetter.class))
                .map(NativeInstanceMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    private List<MethodReference> loadStaticMethods() {
        return Arrays.stream(this.clazz.getMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> !m.isAnnotationPresent(Getter.class))
                .filter(m -> !m.isAnnotationPresent(Setter.class))
                .map(NativeStaticMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    private List<IndexOperation> loadIndexes() {
        List<IndexOperation> operations = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            IndexGetter getter = method.getAnnotation(IndexGetter.class);
            if (getter != null) {
                if (method.getParameterCount() != 1) {
                    throw new InternalException(String.format("Method %s has invalid @IndexGetter. It should have only 1 parameter.", method.getName()));
                }
                if (method.getReturnType() == void.class) {
                    throw new InternalException(String.format("Method %s has invalid @IndexGetter. It cannot return void.", method.getName()));
                }
                SType indexType = SType.fromJavaType(method.getParameters()[0].getType());
                SType returnType = SType.fromJavaType(method.getReturnType());
                if (operations.stream().anyMatch(o -> o.indexType.equals(indexType))) {
                    throw new InternalException(String.format("Method %s has invalid @IndexGetter. @IndexGetter for type %s already defined.", method.getName(), indexType.toString()));
                }
                operations.add(new MethodIndexOperation(method, indexType, returnType));
            }
        }

        return operations;
    }

    private void validateProperty(Method getter, Method setter) {
        if (getter != null) {
            if (getter.getReturnType() == void.class) {
                throw new InternalException();
            }
            if (getter.getParameterCount() != 0) {
                throw new InternalException();
            }
        }

        if (setter != null) {
            if (setter.getReturnType() != void.class) {
                throw new InternalException();
            }
            if (setter.getParameterCount() != 1) {
                throw new InternalException();
            }
        }

        if (getter != null && setter != null) {
            if (getter.getReturnType() != setter.getParameterTypes()[0]) {
                throw new InternalException();
            }
        }
    }

    private static class MethodIndexOperation extends IndexOperation {

        private final Method method;

        public MethodIndexOperation(Method method, SType indexType, SType returnType) {
            super(indexType, returnType);
            this.method = method;
        }

        @Override
        public boolean canGet() {
            return true;
        }

        @Override
        public void compileGet(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(method.getDeclaringClass()),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
        }

        @Override
        public void compileSet(MethodVisitor visitor) {
            throw new InternalException();
        }
    }
}