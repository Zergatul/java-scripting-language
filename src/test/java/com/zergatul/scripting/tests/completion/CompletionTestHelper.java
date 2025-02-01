package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class CompletionTestHelper {

    public static void assertSuggestions(List<Suggestion> expected, List<Suggestion> actual) {
        Assertions.assertEquals(expected.size(), actual.size());

        expected = new ArrayList<>(expected);
        actual = new ArrayList<>(actual);
        while (!expected.isEmpty()) {
            int index = -1;
            for (int i = 0; i < expected.size(); i++) {
                if (expected.get(0).equals(actual.get(i))) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                Assertions.fail();
            }
            expected.remove(0);
            actual.remove(index);
        }
    }
}