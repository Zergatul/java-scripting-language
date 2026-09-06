package com.zergatul.scripting.highlighting;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class SemanticToken {

    private final SemanticTokenType type;
    private final List<SemanticTokenModifier> modifiers;
    private final TextRange range;

    public SemanticToken(SemanticTokenType type, List<SemanticTokenModifier> modifiers, TextRange range) {
        this.type = type;
        this.modifiers = modifiers;
        this.range = range;
    }

    public SemanticToken(SemanticTokenType type, TextRange range) {
        this(type, Lists.of(), range);
    }

    public SemanticTokenType type() {
        return type;
    }

    public List<SemanticTokenModifier> modifiers() {
        return modifiers;
    }

    public TextRange range() {
        return range;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SemanticToken that = (SemanticToken) obj;
        return  Objects.equals(this.type, that.type) &&
                Objects.equals(this.modifiers, that.modifiers) &&
                Objects.equals(this.range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, modifiers, range);
    }

    @Override
    public String toString() {
        return  "SemanticToken[" +
                "type=" + type + ", " +
                "modifiers=" + modifiers + ", " +
                "range=" + range + ']';
    }
}