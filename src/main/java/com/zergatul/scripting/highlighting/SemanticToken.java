package com.zergatul.scripting.highlighting;

import com.zergatul.scripting.TextRange;

import java.util.List;

public record SemanticToken(SemanticTokenType type, List<SemanticTokenModifier> modifiers, TextRange range) {
    public SemanticToken(SemanticTokenType type, TextRange range) {
        this(type, List.of(), range);
    }
}