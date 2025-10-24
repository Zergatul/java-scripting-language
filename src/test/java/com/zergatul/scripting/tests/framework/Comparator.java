package com.zergatul.scripting.tests.framework;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitNode;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.hover.HoverProvider;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenQueue;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SType;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Comparator {

    private final ComparatorRegistry registry;

    public Comparator(ComparatorRegistry registry) {
        this.registry = registry;
    }

    public void assertEquals(BoundCompilationUnitNode expected, BoundCompilationUnitNode actual) {
        assertEquals("unit", expected, actual);
    }

    public void assertEquals(CompilationUnitNode expected, CompilationUnitNode actual) {
        assertEquals("unit", expected, actual);
    }

    public void assertEquals(List<DiagnosticMessage> expected, List<DiagnosticMessage> actual) {
        assertEquals("diagnostics", expected, actual);
    }

    public void assertEquals(List<Token> expected, TokenQueue actual) {
        List<Token> actualList = new ArrayList<>();
        actual.iterator().forEachRemaining(actualList::add);
        assertEquals("tokens", expected, actualList);
    }

    public void assertEquals(HoverProvider.HoverResponse expected, HoverProvider.HoverResponse actual) {
        assertEquals("hover", expected, actual);
    }

    public void assertSemanticEquals(List<SemanticToken> expected, List<SemanticToken> actual) {
        assertEquals("tokens", expected, actual);
    }

    private void assertEquals(String prefix, Object expected, Object actual) {
        List<Difference> differences = new ArrayList<>();
        compare(differences, prefix, expected, actual);
        if (differences.isEmpty()) {
            return;
        }

        Difference first = differences.getFirst();
        Assertions.fail(String.format("%s: %s", first.path(), first.message()));
    }

    private void compare(List<Difference> out, String path, Object a, Object b) {
        if (a == b) {
            return;
        }

        if (a == null || b == null) {
            out.add(new Difference(path, "null/non-null"));
            return;
        }

        Class<?> ca = a.getClass();
        Class<?> cb = b.getClass();

        if (ca.isArray() && cb.isArray()) {
            compareArrays(out, path, a, b);
            return;
        }

        if (List.class.isAssignableFrom(ca) && List.class.isAssignableFrom(cb)) {
            compareLists(out, path, (List<?>) a, (List<?>) b);
            return;
        }

        if (!ca.equals(cb)) {
            out.add(new Difference(path, String.format("Class mismatch: %s / %s", ca.getName(), cb.getName())));
            return;
        }

        if (isAtomic(ca)) {
            if (!a.equals(b)) {
                out.add(new Difference(path, String.format("Value mismatch: expected=%s actual=%s", a, b)));
            }
            return;
        }

        // If we have extractors for this type (or its supertypes) – use them
        List<LabeledExtractor<Object, ?>> extractors = registry.chainFor(ca);
        if (extractors.isEmpty()) {
            throw new InternalException();
        }

        for (LabeledExtractor<Object, ?> ex : extractors) {
            String nextPath = path.isEmpty() ? ex.pathSegment() : path + "." + ex.pathSegment();
            Object va = ex.apply(a);
            Object vb = ex.apply(b);
            compare(out, nextPath, va, vb);
        }
    }

    private void compareArrays(List<Difference> out, String path, Object a, Object b) {
        int lenA = Array.getLength(a);
        int lenB = Array.getLength(b);
        if (lenA != lenB) {
            out.add(new Difference(path + ".length", String.format("Array length mismatch: %s / %s", lenA, lenB)));
            return;
        }

        for (int i = 0; i < lenA; i++) {
            Object ea = Array.get(a, i);
            Object eb = Array.get(b, i);
            compare(out, path + "[" + i + "]", ea, eb);
        }
    }

    private void compareLists(List<Difference> out, String path, List<?> a, List<?> b) {
        if (a.size() != b.size()) {
            out.add(new Difference(path + ".size", String.format("List size mismatch: %s / %s", a.size(), b.size())));
            return;
        }

        for (int i = 0; i < a.size(); i++) {
            Object ea = a.get(i);
            Object eb = b.get(i);
            compare(out, path + "[" + i + "]", ea, eb);
        }
    }

    private boolean isAtomic(Class<?> clazz) {
        if (clazz == Integer.class) {
            return true;
        }
        if (clazz == String.class) {
            return true;
        }
        if (clazz == Method.class) {
            return true;
        }
        if (SType.class.isAssignableFrom(clazz)) {
            return true;
        }
        if (SymbolRef.class.isAssignableFrom(clazz)) {
            return true;
        }
        return clazz.isEnum();
    }
}