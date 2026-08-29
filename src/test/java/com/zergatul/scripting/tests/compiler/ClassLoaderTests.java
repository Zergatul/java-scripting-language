package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.Compiler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ClassLoaderTests {

    @Test
    public void basicTest() throws InterruptedException {
        References references = compileAndRelease("");
        awaitCollected(List.of(references));
    }

    @Test
    public void generatedClassesUseCompilerClassLoader() {
        Runnable script = compile(ApiRoot.class, "");
        Assertions.assertSame(
                Compiler.class.getClassLoader(),
                script.getClass().getClassLoader().getParent());
        Assertions.assertDoesNotThrow(script::run);
    }

    @Test
    public void replacedScriptClassLoadersCanBeCollected() throws InterruptedException {
        ReplacementBatch batch = compileReplacements(32);

        Assertions.assertDoesNotThrow(batch.current::run);
        awaitCollected(batch.oldReferences);
    }

    private static References compileAndRelease(String code) {
        Runnable script = compile(ApiRoot.class, code);
        References references = new References(
                new WeakReference<>(script.getClass()),
                new WeakReference<>(script.getClass().getClassLoader()));

        script.run();
        return references;
    }

    private static ReplacementBatch compileReplacements(int count) {
        List<References> references = new ArrayList<>();
        Runnable current = compile(ApiRoot.class, "int value = 0;");

        for (int i = 1; i < count; i++) {
            references.add(new References(
                    new WeakReference<>(current.getClass()),
                    new WeakReference<>(current.getClass().getClassLoader())));
            current = compile(ApiRoot.class, "int value = " + i + ";");
        }

        return new ReplacementBatch(current, references);
    }

    @SuppressWarnings("removal")
    private static void awaitCollected(List<References> references) throws InterruptedException {
        byte[][] pressure = new byte[4][];
        for (int attempt = 0; attempt < 40; attempt++) {
            if (references.stream().allMatch(References::isCollected)) {
                return;
            }

            pressure[attempt % pressure.length] = new byte[256 * 1024];
            System.gc();
            System.runFinalization();
            Thread.sleep(25);
        }

        long classes = references.stream().filter(reference -> reference.scriptClass.get() != null).count();
        long classLoaders = references.stream().filter(reference -> reference.classLoader.get() != null).count();
        Assertions.fail("Generated classes still reachable: " + classes + "; classloaders still reachable: " + classLoaders + ".");
    }

    private record References(WeakReference<Class<?>> scriptClass, WeakReference<ClassLoader> classLoader) {
        public boolean isCollected() {
            return scriptClass.get() == null && classLoader.get() == null;
        }
    }

    private record ReplacementBatch(Runnable current, List<References> oldReferences) {}

    public static class ApiRoot {}
}