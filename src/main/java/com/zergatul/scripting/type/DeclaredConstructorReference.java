package com.zergatul.scripting.type;

import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class DeclaredConstructorReference extends ConstructorReference {

    private final SDeclaredType classType;
    private final SFunction constructorType;

    public DeclaredConstructorReference(SDeclaredType classType) {
        this(classType, new SFunction(SVoidType.instance, new MethodParameter[0]));
    }

    public DeclaredConstructorReference(SDeclaredType classType, SFunction constructorType) {
        this.classType = classType;
        this.constructorType = constructorType;
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                classType.getInternalName(),
                "<init>",
                constructorType.getDescriptor(),
                false);
    }

    @Override
    public List<MethodParameter> getParameters() {
        return constructorType.getParameters();
    }
}
