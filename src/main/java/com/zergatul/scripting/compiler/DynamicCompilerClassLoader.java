package com.zergatul.scripting.compiler;

import com.zergatul.scripting.old.compiler.ScriptingClassLoader;

public class DynamicCompilerClassLoader extends ClassLoader {

    public DynamicCompilerClassLoader() {
        super(ScriptingClassLoader.class.getClassLoader());
    }

    public Class<?> defineClass(String name, byte[] code) {
        return defineClass(name, code, 0, code.length);
    }
}