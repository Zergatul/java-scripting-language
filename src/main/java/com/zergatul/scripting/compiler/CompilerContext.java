package com.zergatul.scripting.compiler;

import com.zergatul.scripting.type.SFloatType;
import com.zergatul.scripting.type.SType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class CompilerContext {

    private final CompilerContext root;
    private final List<Symbol> staticSymbols;
    private int stackIndex;

    public CompilerContext(int initialStackIndex) {
        this.root = this;
        this.stackIndex = initialStackIndex;
        this.staticSymbols = new ArrayList<>();
    }

    public CompilerContext(Class<?> root, int initialStackIndex) {
        this(initialStackIndex);
        parseStaticFields(root);
    }

    public void addStaticConstant(String name, SType type, Object value) {

    }

    public LocalVariable addLocalVariable(String name, SType type) {
        LocalVariable variable = new LocalVariable(name, type, stackIndex);
        if (type == SFloatType.instance) {
            stackIndex += 2;
        } else {
            stackIndex += 1;
        }
        return variable;
    }

    public Symbol getSymbol(String name) {
        return null;
    }

    private void parseStaticFields(Class<?> root) {
        Field[] fields = root.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
                SType type = SType.fromJavaClass(field.getType());
                if (type == null) {
                    continue;
                }

                Object value;
                try {
                    value = field.get(null);
                } catch (IllegalAccessException e) {
                    continue;
                }

                addStaticConstant(field.getName(), type, value);
            }
        }
    }
}