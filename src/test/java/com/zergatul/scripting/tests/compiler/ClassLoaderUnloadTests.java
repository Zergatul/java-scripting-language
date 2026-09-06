package com.zergatul.scripting.tests.compiler;

import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.zergatul.scripting.tests.compiler.helpers.CompilerHelper.compile;

public class ClassLoaderUnloadTests {

    @Test
    public void basicTest() throws InterruptedException {
        References references = compileAndRelease("");

        awaitCollected(Lists.of(references));
    }

    @Test
    public void generatedClassesTargetJava17AndUseCompilerClassLoader() throws ReflectiveOperationException {
        Field field = Compiler.class.getDeclaredField("CLASS_FILE_VERSION");
        field.setAccessible(true);
        Assertions.assertEquals(Opcodes.V1_8, field.getInt(null));

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

    private static final class References {

        private final WeakReference<Class<?>> scriptClass;
        private final WeakReference<ClassLoader> classLoader;

        private References(
                WeakReference<Class<?>> scriptClass,
                WeakReference<ClassLoader> classLoader
        ) {
            this.scriptClass = scriptClass;
            this.classLoader = classLoader;
        }

        public boolean isCollected() {
            return scriptClass.get() == null && classLoader.get() == null;
        }

        public WeakReference<Class<?>> scriptClass() {
            return scriptClass;
        }

        public WeakReference<ClassLoader> classLoader() {
            return classLoader;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            References that = (References) obj;
            return  Objects.equals(this.scriptClass, that.scriptClass) &&
                    Objects.equals(this.classLoader, that.classLoader);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scriptClass, classLoader);
        }

        @Override
        public String toString() {
            return  "References[" +
                    "scriptClass=" + scriptClass + ", " +
                    "classLoader=" + classLoader + ']';
        }
    }

    private static final class ReplacementBatch {

        private final Runnable current;
        private final List<References> oldReferences;

        private ReplacementBatch(Runnable current, List<References> oldReferences) {
            this.current = current;
            this.oldReferences = oldReferences;
        }

        public Runnable current() {
            return current;
        }

        public List<References> oldReferences() {
            return oldReferences;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            ReplacementBatch that = (ReplacementBatch) obj;
            return  Objects.equals(this.current, that.current) &&
                    Objects.equals(this.oldReferences, that.oldReferences);
        }

        @Override
        public int hashCode() {
            return Objects.hash(current, oldReferences);
        }

        @Override
        public String toString() {
            return  "ReplacementBatch[" +
                    "current=" + current + ", " +
                    "oldReferences=" + oldReferences + ']';
        }
    }

    public static class ApiRoot {}
}