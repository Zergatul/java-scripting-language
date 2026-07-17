package com.zergatul.scripting.completion;

import org.jspecify.annotations.Nullable;

public record SuggestionInfo(
        String label,
        @Nullable String detail,
        @Nullable String documentation,
        String insertText,
        SuggestionKind kind
) {}