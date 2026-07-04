package com.zergatul.scripting.tests.highlighting.helpers;

import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.highlighting.HighlightingProvider;
import com.zergatul.scripting.highlighting.SemanticToken;

import java.util.List;

public class HighlightingHelper {

    public static List<SemanticToken> highlight(Class<?> api, String code) {
        CompilationParameters parameters = new CompilationParametersBuilder().setRoot(api).build();
        AnalysisResult result = new Analyzer().analyze(code, parameters);
        return new HighlightingProvider(result.lexerOutput(), result.binderOutput()).get();
    }
}