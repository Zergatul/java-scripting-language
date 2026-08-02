package com.zergatul.scripting.tests.hover;

import com.zergatul.scripting.tests.compiler.helpers.IntStorage;
import org.junit.jupiter.api.Test;

import java.util.List;

public class JavaTypeParameterTests {

    @Test
    public void exactTest1() {
        assertHover("""
                lists.<cursor>getListOfIntegers();
                """,
                17,
                List.of("Java<java.util.List><Boxed<int>> com.zergatul.scripting.tests.hover.JavaTypeParameterTests$ListsApi.getListOfIntegers()"));
    }

    @Test
    public void exactTest2() {
        assertHover("""
                lists.<cursor>getListOfStrings();
                """,
                16,
                List.of("Java<java.util.List><string> com.zergatul.scripting.tests.hover.JavaTypeParameterTests$ListsApi.getListOfStrings()"));
    }

    @Test
    public void exactTest3() {
        assertHover("""
                lists.<cursor>getListOfListOfStrings();
                """,
                22,
                List.of("Java<java.util.List><Java<java.util.List><string>> com.zergatul.scripting.tests.hover.JavaTypeParameterTests$ListsApi.getListOfListOfStrings()"));
    }

    private static void assertHover(String code, int length, List<String> expected) {
        HoverTestHelper.assertHover(code, ApiRoot.class, length, expected);
    }

    public static class ApiRoot {
        public static IntStorage intStorage;
        public static ListsApi lists;
    }

    @SuppressWarnings("unused")
    public static class ListsApi {

        public List<Integer> getListOfIntegers() {
            return List.of();
        }

        public List<String> getListOfStrings() {
            return List.of();
        }

        public List<List<String>> getListOfListOfStrings() {
            return List.of();
        }
    }
}