package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.SUnknown;
import org.objectweb.asm.MethodVisitor;

public class UndefinedIndexOperation extends IndexOperation {

    public static final IndexOperation instance = new UndefinedIndexOperation();

    private UndefinedIndexOperation() {
        super(SUnknown.instance, SUnknown.instance);
    }

    @Override
    public void compileGet(MethodVisitor visitor) {
        throw new InternalException();
    }

    @Override
    public void compileSet(MethodVisitor visitor) {
        throw new InternalException();
    }
}