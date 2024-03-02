package com.zergatul.scripting.old.compiler;

public class ScriptingClassLoader extends ClassLoader {

    public ScriptingClassLoader() {
        super(ScriptingClassLoader.class.getClassLoader());
    }

    public Class<?> defineClass(String name, byte[] code) {
        return defineClass(name, code, 0, code.length);
    }
}