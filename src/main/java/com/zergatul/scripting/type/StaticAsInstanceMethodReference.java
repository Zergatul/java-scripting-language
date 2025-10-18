package com.zergatul.scripting.type;

import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class StaticAsInstanceMethodReference extends MethodReference {

    private final Class<?> ownerClass;
    private final SType ownerType;
    private final String underlyingName;
    private final String name;
    private final SType returnType;
    private final MethodParameter[] parameters;
    private final Optional<String> description;

    public StaticAsInstanceMethodReference(Class<?> ownerClass, SType ownerType, String name, SType returnType, MethodParameter... parameters) {
        this(Optional.empty(), ownerClass, ownerType, name, name, returnType, parameters);
    }

    public StaticAsInstanceMethodReference(String description, Class<?> ownerClass, SType ownerType, String underlyingName, String name, SType returnType, MethodParameter... parameters) {
        this(Optional.of(description), ownerClass, ownerType, underlyingName, name, returnType, parameters);
    }

    public StaticAsInstanceMethodReference(String description, Class<?> ownerClass, SType ownerType, String name, SType returnType, MethodParameter... parameters) {
        this(Optional.of(description), ownerClass, ownerType, name, name, returnType, parameters);
    }

    private StaticAsInstanceMethodReference(
            Optional<String> description,
            Class<?> ownerClass,
            SType ownerType,
            String underlyingName,
            String name,
            SType returnType,
            MethodParameter... parameters
    ) {
        this.description = description;
        this.ownerClass = ownerClass;
        this.ownerType = ownerType;
        this.underlyingName = underlyingName;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public SType getOwner() {
        return ownerType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SType getReturn() {
        return returnType;
    }

    @Override
    public List<MethodParameter> getParameters() {
        return List.of(parameters);
    }

    @Override
    public Optional<String> getDescription() {
        if (this.description.isPresent()) {
            return this.description;
        } else {
            Optional<Method> opt = Arrays.stream(ownerClass.getDeclaredMethods())
                    .filter(m -> m.getName().equals(underlyingName))
                    .filter(m -> Modifier.isPublic(m.getModifiers()))
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .filter(m -> m.getReturnType() == returnType.getJavaClass())
                    .filter(m -> m.getParameterCount() == parameters.length + 1)
                    .filter(m -> {
                        Class<?>[] types = m.getParameterTypes();
                        if (types[0] != ownerType.getJavaClass()) {
                            return false;
                        }
                        for (int i = 0; i < parameters.length; i++) {
                            if (types[i + 1] != parameters[i].type().getJavaClass()) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .findFirst();

            if (opt.isEmpty()) {
                return Optional.empty();
            } else {
                Method method = opt.get();
                MethodDescription description = method.getAnnotation(MethodDescription.class);
                return description != null ? Optional.of(description.value()) : Optional.empty();
            }
        }
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(ownerClass),
                underlyingName,
                Type.getMethodDescriptor(
                        Type.getType(returnType.getJavaClass()),
                        Stream.concat(Stream.of(ownerType), getParameterTypes().stream())
                                .map(SType::getJavaClass)
                                .map(Type::getType)
                                .toArray(Type[]::new)),
                false);
    }
}
