package com.zergatul.scripting.tests.completion.helpers;

import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.tests.completion.suggestions.CustomTypeSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.KeywordSuggestion;
import com.zergatul.scripting.tests.completion.suggestions.Suggestion;
import com.zergatul.scripting.tests.completion.suggestions.TypeSuggestion;
import com.zergatul.scripting.type.*;

import java.util.List;

public class CommonSuggestions {

    public static final List<Suggestion> unitMembers = List.of(
            new KeywordSuggestion(TokenType.STATIC),
            new KeywordSuggestion(TokenType.VOID),
            new KeywordSuggestion(TokenType.CLASS));

    public static final List<Suggestion> types = List.of(
            new TypeSuggestion(SBoolean.instance),
            new TypeSuggestion(SInt16.instance),
            new TypeSuggestion(SInt.instance),
            new TypeSuggestion(SInt64.instance),
            new TypeSuggestion(SChar.instance),
            new TypeSuggestion(SFloat32.instance),
            new TypeSuggestion(SFloat.instance),
            new TypeSuggestion(SString.instance),
            new CustomTypeSuggestion(RuntimeType.class));

    public static final List<Suggestion> expressions = Lists.of(
            types,
            new KeywordSuggestion(TokenType.META_TYPE),
            new KeywordSuggestion(TokenType.META_TYPE_OF));

    public static final List<Suggestion> statements = Lists.of(
            expressions,
            new KeywordSuggestion(TokenType.LET),
            new KeywordSuggestion(TokenType.FOR),
            new KeywordSuggestion(TokenType.FOREACH),
            new KeywordSuggestion(TokenType.IF),
            new KeywordSuggestion(TokenType.WHILE),
            new KeywordSuggestion(TokenType.RETURN));

    public static final List<Suggestion> loopStatements = Lists.of(
            expressions,
            new KeywordSuggestion(TokenType.LET),
            new KeywordSuggestion(TokenType.FOR),
            new KeywordSuggestion(TokenType.FOREACH),
            new KeywordSuggestion(TokenType.IF),
            new KeywordSuggestion(TokenType.WHILE),
            new KeywordSuggestion(TokenType.RETURN),
            new KeywordSuggestion(TokenType.BREAK),
            new KeywordSuggestion(TokenType.CONTINUE));
}