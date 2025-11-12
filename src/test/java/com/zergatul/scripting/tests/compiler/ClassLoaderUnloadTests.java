package com.zergatul.scripting.tests.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ClassLoaderUnloadTests {

    @Test
    public void basicTest() {
        Runnable script = compile(ApiRoot.class, "");
        WeakReference<Class<?>> scriptRef = new WeakReference<>(script.getClass());
        WeakReference<ClassLoader> loaderRef = new WeakReference<>(script.getClass().getClassLoader());

        script.run();
        script = null;

        System.gc();

        Assertions.assertNull(scriptRef.get());
        Assertions.assertNull(loaderRef.get());
    }

    public static class ApiRoot {}
}