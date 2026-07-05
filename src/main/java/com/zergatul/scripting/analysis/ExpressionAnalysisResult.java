package com.zergatul.scripting.analysis;

import com.zergatul.scripting.binding.BinderExpressionOutput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.ParserExpressionOutput;

public record ExpressionAnalysisResult(
        LexerOutput lexerOutput,
        ParserExpressionOutput parserOutput,
        BinderExpressionOutput binderOutput
) {}