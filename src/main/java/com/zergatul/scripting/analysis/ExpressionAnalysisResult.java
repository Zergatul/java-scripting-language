package com.zergatul.scripting.analysis;

import com.zergatul.scripting.binding.BinderExpressionOutput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.ParserExpressionOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class ExpressionAnalysisResult {

    private final LexerOutput lexerOutput;
    private final ParserExpressionOutput parserOutput;
    private final BinderExpressionOutput binderOutput;

    public ExpressionAnalysisResult(
            LexerOutput lexerOutput,
            ParserExpressionOutput parserOutput,
            BinderExpressionOutput binderOutput
    ) {
        this.lexerOutput = lexerOutput;
        this.parserOutput = parserOutput;
        this.binderOutput = binderOutput;
    }

    public LexerOutput lexerOutput() {
        return lexerOutput;
    }

    public ParserExpressionOutput parserOutput() {
        return parserOutput;
    }

    public BinderExpressionOutput binderOutput() {
        return binderOutput;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ExpressionAnalysisResult that = (ExpressionAnalysisResult) obj;
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
        return  "ExpressionAnalysisResult[" +
                "lexerOutput=" + lexerOutput + ", " +
                "parserOutput=" + parserOutput + ", " +
                "binderOutput=" + binderOutput + ']';
    }
}