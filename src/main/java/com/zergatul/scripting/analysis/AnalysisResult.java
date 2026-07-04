package com.zergatul.scripting.analysis;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.ParserOutput;

public record AnalysisResult(LexerOutput lexerOutput, ParserOutput parserOutput, BinderOutput binderOutput) {}