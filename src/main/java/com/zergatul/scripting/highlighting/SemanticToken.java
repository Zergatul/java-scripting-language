package com.zergatul.scripting.highlighting;

import com.zergatul.scripting.TextRange;

public record SemanticToken(SemanticTokenType type, TextRange range) {}