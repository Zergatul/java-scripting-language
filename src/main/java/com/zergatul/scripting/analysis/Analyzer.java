package com.zergatul.scripting.analysis;

import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderExpressionOutput;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserExpressionOutput;
import com.zergatul.scripting.parser.ParserOutput;

public class Analyzer {

    public AnalysisResult analyze(String code, CompilationParameters parameters) {
        LexerOutput lexerOutput = new Lexer(new LexerInput(code)).lex();
        ParserOutput parserOutput = new Parser(lexerOutput).parse();
        BinderOutput binderOutput = Binder.bind(parserOutput, parameters);
        return new AnalysisResult(lexerOutput, parserOutput, binderOutput);
    }

    public ExpressionAnalysisResult analyzeAsExpression(String code, CompilationParameters parameters) {
        LexerOutput lexerOutput = new Lexer(new LexerInput(code)).lex();
        ParserExpressionOutput parserOutput = new Parser(lexerOutput).parseAsExpression();
        BinderExpressionOutput binderOutput = Binder.bind(parserOutput, parameters);
        return new ExpressionAnalysisResult(lexerOutput, parserOutput, binderOutput);
    }
}