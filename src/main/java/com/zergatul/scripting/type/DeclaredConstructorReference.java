package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class DeclaredConstructorReference extends ConstructorReference {

    private final SDeclaredType classType;
    private final SStaticFunction constructorType;

    public DeclaredConstructorReference(SDeclaredType classType) {
        this(classType, new SStaticFunction(SVoidType.instance, new MethodParameter[0]));
    }

    public DeclaredConstructorReference(SDeclaredType classType, SStaticFunction constructorType) {
        this.classType = classType;
        this.constructorType = constructorType;
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                classType.getInternalName(),
                "<init>",
                constructorType.getMethodDescriptor(),
                false);
    }

    @Override
    public List<MethodParameter> getParameters() {
        return constructorType.getParameters();
    }
}
