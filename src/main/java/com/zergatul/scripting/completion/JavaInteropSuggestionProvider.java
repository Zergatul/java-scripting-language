package com.zergatul.scripting.completion;

import java.util.List;

@FunctionalInterface
public interface JavaInteropSuggestionProvider {

    /**
     * Returns packages and classes that are direct children of {@code prefix}.
     * The prefix is case-sensitive, excludes the trailing dot, and is empty for
     * suggestions in the root/default package.
     */
    List<ClassSuggestion> suggest(String prefix);
}