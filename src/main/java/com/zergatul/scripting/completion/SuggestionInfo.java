package com.zergatul.scripting.completion;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class SuggestionInfo {

    private final String label;
    private final @Nullable String detail;
    private final @Nullable String documentation;
    private final String insertText;
    private final SuggestionKind kind;

    public SuggestionInfo(
            String label,
            @Nullable String detail,
            @Nullable String documentation,
            String insertText,
            SuggestionKind kind
    ) {
        this.label = label;
        this.detail = detail;
        this.documentation = documentation;
        this.insertText = insertText;
        this.kind = kind;
    }

    public String label() {
        return label;
    }

    @Nullable
    public String detail() {
        return detail;
    }

    @Nullable
    public String documentation() {
        return documentation;
    }

    public String insertText() {
        return insertText;
    }

    public SuggestionKind kind() {
        return kind;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SuggestionInfo that = (SuggestionInfo) obj;
        return  Objects.equals(this.label, that.label) &&
                Objects.equals(this.detail, that.detail) &&
                Objects.equals(this.documentation, that.documentation) &&
                Objects.equals(this.insertText, that.insertText) &&
                Objects.equals(this.kind, that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, detail, documentation, insertText, kind);
    }

    @Override
    public String toString() {
        return  "SuggestionInfo[" +
                "label=" + label + ", " +
                "detail=" + detail + ", " +
                "documentation=" + documentation + ", " +
                "insertText=" + insertText + ", " +
                "kind=" + kind + ']';
    }
}