package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

public class UnknownConstructorReference extends ConstructorReference {

    public static final UnknownConstructorReference instance = new UnknownConstructorReference();

    private UnknownConstructorReference() {
        super(null);
    }

    @Override
    public void compileInvoke(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public List<MethodParameter> getParameters() {
        throw new InternalException();
    }
}