package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.GenericDeclaration;
import java.util.List;

// An unresolved/open E becomes SJavaTypeVariable
// A method-owned variable such as <T> T convert(...) remains SJavaTypeVariable until method type arguments are inferred or supplied.
public class SJavaTypeVariable extends SReferenceType {

    private final GenericDeclaration declaration;
    private final String name;
    private final List<SType> upperBounds;
    private final Class<?> erasedClass;

    public SJavaTypeVariable(GenericDeclaration declaration, String name, List<SType> upperBounds, Class<?> erasedClass) {
        this.declaration = declaration;
        this.name = name;
        this.upperBounds = upperBounds;
        this.erasedClass = erasedClass;
    }

    @Override
    public Class<?> getJavaClass() {
        return erasedClass;
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public void storeDefaultValue(MethodVisitor visitor) {
        throw new InternalException();
    }
}