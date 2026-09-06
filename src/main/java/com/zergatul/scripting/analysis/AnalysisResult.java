package com.zergatul.scripting.analysis;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.ParserOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class AnalysisResult {

    private final LexerOutput lexerOutput;
    private final ParserOutput parserOutput;
    private final BinderOutput binderOutput;

    public AnalysisResult(LexerOutput lexerOutput, ParserOutput parserOutput, BinderOutput binderOutput) {
        this.lexerOutput = lexerOutput;
        this.parserOutput = parserOutput;
        this.binderOutput = binderOutput;
    }

    public LexerOutput lexerOutput() {
        return lexerOutput;
    }

    public ParserOutput parserOutput() {
        return parserOutput;
    }

    public BinderOutput binderOutput() {
        return binderOutput;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        AnalysisResult that = (AnalysisResult) obj;
        return  Objects.equals(this.lexerOutput, that.lexerOutput) &&
                Objects.equals(this.parserOutput, that.parserOutput) &&
                Objects.equals(this.binderOutput, that.binderOutput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lexerOutput, parserOutput, binderOutput);
    }

    @Override
    public String toString() {
        return  "AnalysisResult[" +
                "lexerOutput=" + lexerOutput + ", " +
                "parserOutput=" + parserOutput + ", " +
                "binderOutput=" + binderOutput + ']';
    }
}