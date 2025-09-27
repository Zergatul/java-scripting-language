package com.zergatul.scripting.lexer;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;

import java.util.Arrays;
import java.util.List;

public class Token implements Locatable {

    protected static final Trivia[] EMPTY_TRIVIA = new Trivia[0];

    private final TokenType tokenType;
    protected final Trivia[] leadingTrivia;
    protected final Trivia[] trailingTrivia;
    private final TextRange range;

    public Token(TokenType tokenType, TextRange range) {
        this(tokenType, List.of(), List.of(), range);
    }

    public Token(TokenType tokenType, List<Trivia> leadingTrivia, List<Trivia> trailingTrivia, TextRange range) {
        this(tokenType, toArray(leadingTrivia), toArray(trailingTrivia), range);
    }

    protected Token(TokenType tokenType, Trivia[] leadingTrivia, Trivia[] trailingTrivia, TextRange range) {
        this.tokenType = tokenType;
        this.range = range;
        this.leadingTrivia = leadingTrivia;
        this.trailingTrivia = trailingTrivia;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public TextRange getRange() {
        return range;
    }

    public List<Trivia> getLeadingTrivia() {
        return Arrays.asList(leadingTrivia);
    }

    public List<Trivia> getTrailingTrivia() {
        return Arrays.asList(trailingTrivia);
    }

    public boolean is(TokenType tokenType) {
        return this.tokenType == tokenType;
    }

    public boolean isNot(TokenType tokenType) {
        return this.tokenType != tokenType;
    }

    public String getRawValue(String code) {
        return getRange().extract(code);
    }

    public Token withLeadingTrivia(Trivia trivia) {
        return withLeadingTrivia(List.of(trivia));
    }

    public Token withLeadingTrivia(List<Trivia> trivia) {
        if (this.getClass() != Token.class) {
            throw new InternalException();
        }

        return new Token(tokenType, merge(leadingTrivia, trivia), trailingTrivia, range);
    }

    public Token withTrailingTrivia(Trivia trivia) {
        if (this.getClass() != Token.class) {
            throw new InternalException();
        }

        return new Token(tokenType, leadingTrivia, merge(trailingTrivia, trivia), range);
    }

    public String asFullSource() {
        StringBuilder builder = new StringBuilder();
        for (var trivia : leadingTrivia) {
            builder.append(trivia.asSource());
        }
        builder.append(asFullSource());
        for (var trivia : trailingTrivia) {
            builder.append(trivia.asSource());
        }
        return builder.toString();
    }

    protected String asSource() {
        return switch (tokenType) {
            case LEFT_PARENTHESES -> "(";
            case RIGHT_PARENTHESES -> ")";
            case LEFT_SQUARE_BRACKET -> "[";
            case RIGHT_SQUARE_BRACKET -> "]";
            case LEFT_CURLY_BRACKET -> "{";
            case RIGHT_CURLY_BRACKET -> "}";
            case DOT -> ".";
            case DOLLAR -> "$";
            case COMMA -> ",";
            case COLON -> ":";
            case SEMICOLON -> ";";
            case EXCLAMATION -> "!";
            case AMPERSAND -> "&";
            case PIPE -> "|";
            case PLUS -> "+";
            case PLUS_PLUS -> "++";
            case MINUS -> "-";
            case MINUS_MINUS -> "--";
            case ASTERISK -> "*";
            case SLASH -> "/";
            case PERCENT -> "%";
            case LESS -> "<";
            case GREATER -> ">";
            case FALSE -> "false";
            case TRUE -> "true";
            case END_OF_FILE -> "";
            case EQUAL -> "=";
            case QUESTION -> "?";
            case BOOLEAN -> "boolean";
            case INT8 -> "int8";
            case INT16 -> "int16";
            case INT -> "int";
            case INT32 -> "int32";
            case INT64 -> "int64";
            case LONG -> "long";
            case FLOAT32 -> "float32";
            case FLOAT -> "float";
            case FLOAT64 -> "float64";
            case CHAR -> "char";
            case NEW -> "new";
            case EQUAL_EQUAL -> "==";
            case EQUAL_GREATER -> "=>";
            case EXCLAMATION_EQUAL -> "!=";
            case AMPERSAND_AMPERSAND -> "&&";
            case AMPERSAND_EQUAL -> "&=";
            case PIPE_PIPE -> "||";
            case PIPE_EQUAL -> "|=";
            case LESS_EQUAL -> "<=";
            case GREATER_EQUAL -> ">=";
            case PLUS_EQUAL -> "+=";
            case MINUS_EQUAL -> "-=";
            case ASTERISK_EQUAL -> "*=";
            case SLASH_EQUAL -> "/=";
            case PERCENT_EQUAL -> "%=";
            case IF -> "if";
            case ELSE -> "else";
            case RETURN -> "return";
            case FOR -> "for";
            case FOREACH -> "foreach";
            case WHILE -> "while";
            case BREAK -> "break";
            case CONTINUE -> "continue";
            case IN -> "in";
            case STATIC -> "static";
            case VOID -> "void";
            case REF -> "ref";
            case ASYNC -> "async";
            case AWAIT -> "await";
            case LET -> "let";
            case IS -> "is";
            case AS -> "as";
            case META_TYPE -> "#type";
            case META_TYPE_OF -> "$typeof";
            case CLASS -> "class";
            case CONSTRUCTOR -> "constructor";
            case THIS -> "this";
            default -> throw new InternalException();
        };
    }

    protected static Trivia[] merge(Trivia[] array1, List<Trivia> array2) {
        Trivia[] result = new Trivia[array1.length + array2.size()];
        System.arraycopy(array1, 0, result, 0, array1.length);
        for (int i = 0; i < array2.size(); i++) {
            result[array1.length + i] = array2.get(i);
        }
        return result;
    }

    protected static Trivia[] merge(Trivia[] array, Trivia trivia) {
        Trivia[] result = new Trivia[array.length + 1];
        System.arraycopy(array, 0, result, 0, array.length);
        result[array.length] = trivia;
        return result;
    }

    protected static Trivia[] toArray(List<Trivia> list) {
        return list.isEmpty() ? EMPTY_TRIVIA : list.toArray(Trivia[]::new);
    }
}