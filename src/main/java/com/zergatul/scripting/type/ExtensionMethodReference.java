package com.zergatul.scripting.type;

import com.zergatul.scripting.compiler.CompilerContext;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;

public class ExtensionMethodReference extends MethodReference {

    private final SType owner;
    private final String name;
    private final SMethodFunction functionType;
    private final String internalName;

    public ExtensionMethodReference(SType owner, String name, SMethodFunction functionType, String internalName) {
        this.owner = owner;
        this.name = name;
        this.functionType = functionType;
        this.internalName = internalName;
    }

    @Override
    public SType getOwner() {
        return owner;
    }

    @Override
    public SType getReturn() {
        return functionType.getReturnType();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return functionType.getParameters();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptor() {
        List<MethodParameter> parameters = functionType.getParameters();
        Type[] argumentTypes = new Type[parameters.size() + 1];
        argumentTypes[0] = owner.getAsmType();
        for (int i = 0; i < parameters.size(); i++) {
            argumentTypes[i + 1] = parameters.get(i).type().getAsmType();
        }
        return Type.getMethodDescriptor(getReturn().getAsmType(), argumentTypes);
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void compileInvoke(MethodVisitor visitor, CompilerContext context) {
        visitor.visitMethodInsn(
                INVOKESTATIC,
                context.getClassName(),
                internalName,
                getDescriptor(),
                false);
    }
}
