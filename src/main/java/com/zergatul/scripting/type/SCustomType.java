package com.zergatul.scripting.type;

import com.zergatul.scripting.*;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.IndexOperation;
import com.zergatul.scripting.type.operation.ReferenceTypeOperations;
import org.jspecify.annotations.Nullable;
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
    private final Lazy<List<PropertyReference>> instanceProperties;
    private final Lazy<List<MethodReference>> instanceMethods;
    private final Lazy<List<IndexOperation>> indexes;
    private final Lazy<List<MethodReference>> staticMethods;
    private final Lazy<List<PropertyReference>> staticProperties;

    public SCustomType(Class<?> clazz) {
        CustomType annotation = clazz.getAnnotation(CustomType.class);
        if (annotation == null) {
            throw new InternalException("Custom type should annotated with @CustomType.");
        }

        this.clazz = clazz;
        this.annotation = annotation;
        this.instanceProperties = new Lazy<>(this::loadInstanceProperties);
        this.instanceMethods = new Lazy<>(this::loadInstanceMethods);
        this.indexes = new Lazy<>(this::loadIndexes);
        this.staticMethods = new Lazy<>(this::loadStaticMethods);
        this.staticProperties = new Lazy<>(this::loadStaticProperties);
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
    public boolean hasDefaultValue() {
        return false;
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
    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    @Override
    public List<ConstructorReference> getConstructors() {
        return Arrays.stream(clazz.getConstructors())
                .map(NativeConstructorReference::new)
                .map(c -> (ConstructorReference) c)
                .toList();
    }

    @Override
    public List<PropertyReference> getInstanceProperties() {
        return instanceProperties.value();
    }

    @Override
    @Nullable
    public PropertyReference getInstanceProperty(String name) {
        return instanceProperties.value().stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
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
    public List<PropertyReference> getStaticProperties() {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .map(StaticFieldPropertyReference::new)
                .map(r -> (PropertyReference) r)
                .toList();
    }

    @Override
    public List<SType> supportedIndexers() {
        return indexes.value().stream().map(o -> o.indexType).toList();
    }

    @Override
    @Nullable
    public IndexOperation index(SType type) {
        return indexes.value().stream().filter(o -> o.indexType.equals(type)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return annotation.name();
    }

    private List<PropertyReference> loadInstanceProperties() {
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
                .filter(m -> !m.isAnnotationPresent(IndexSetter.class))
                .map(NativeInstanceMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    private List<IndexOperation> loadIndexes() {
        List<IndexOperation> operations = new ArrayList<>();
        for (Method getterMethod : clazz.getMethods()) {
            if (Modifier.isStatic(getterMethod.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(getterMethod.getModifiers())) {
                continue;
            }
            if (getterMethod.getDeclaringClass() == Object.class) {
                continue;
            }

            if (!getterMethod.isAnnotationPresent(IndexGetter.class)) {
                continue;
            }

            if (getterMethod.getParameterCount() != 1) {
                throw new InternalException(String.format("Method %s has invalid @IndexGetter. It should have only 1 parameter.", getterMethod.getName()));
            }
            if (getterMethod.getReturnType() == void.class) {
                throw new InternalException(String.format("Method %s has invalid @IndexGetter. It cannot return void.", getterMethod.getName()));
            }
            SType indexType = SType.fromJavaType(getterMethod.getParameters()[0].getType());
            SType returnType = SType.fromJavaType(getterMethod.getReturnType());
            if (operations.stream().anyMatch(o -> o.indexType.equals(indexType))) {
                throw new InternalException(String.format("Method %s has invalid @IndexGetter. @IndexGetter for type %s already defined.", getterMethod.getName(), indexType.toString()));
            }

            // find corresponding setter
            Method setterMethod = Arrays.stream(this.clazz.getMethods())
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .filter(m -> Modifier.isPublic(m.getModifiers()))
                    .filter(m -> m.getDeclaringClass() != Object.class)
                    .filter(m -> m.isAnnotationPresent(IndexSetter.class))
                    .filter(m -> {
                        if (m.getParameterCount() != 2) {
                            return false;
                        }
                        Class<?>[] parameters = m.getParameterTypes();
                        return SType.fromJavaType(parameters[0]).equals(indexType) && SType.fromJavaType(parameters[1]).equals(returnType);
                    })
                    .findFirst()
                    .orElse(null);

            operations.add(new MethodIndexOperation(getterMethod, setterMethod, indexType, returnType));
        }

        return operations;
    }

    private List<MethodReference> loadStaticMethods() {
        return Arrays.stream(this.clazz.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getDeclaringClass() != Object.class)
                .filter(m -> !m.isAnnotationPresent(Getter.class))
                .filter(m -> !m.isAnnotationPresent(Setter.class))
                .map(NativeStaticMethodReference::new)
                .map(r -> (MethodReference) r)
                .toList();
    }

    private List<PropertyReference> loadStaticProperties() {
        List<PropertyReference> list = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            list.add(new StaticFieldPropertyReference(field));
        }

        return list;
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

        private final Method getterMethod;

        @Nullable
        private final Method setterMethod;

        public MethodIndexOperation(
                Method getterMethod,
                @Nullable Method setterMethod,
                SType indexType,
                SType returnType
        ) {
            super(indexType, returnType);
            this.getterMethod = getterMethod;
            this.setterMethod = setterMethod;
        }

        @Override
        public boolean canGet() {
            return true;
        }

        @Override
        public boolean canSet() {
            return setterMethod != null;
        }

        @Override
        public void compileGet(MethodVisitor visitor) {
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(getterMethod.getDeclaringClass()),
                    getterMethod.getName(),
                    Type.getMethodDescriptor(getterMethod),
                    false);
        }

        @Override
        public void compileSet(MethodVisitor visitor) {
            if (setterMethod == null) {
                throw new InternalException();
            }

            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(setterMethod.getDeclaringClass()),
                    setterMethod.getName(),
                    Type.getMethodDescriptor(setterMethod),
                    false);
        }
    }
}