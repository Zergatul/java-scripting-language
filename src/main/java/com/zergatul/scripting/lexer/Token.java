package com.zergatul.scripting.lexer;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token other) {
            return  other.tokenType == tokenType &&
                    Arrays.equals(other.leadingTrivia, leadingTrivia) &&
                    Arrays.equals(other.trailingTrivia, trailingTrivia) &&
                    other.range.equals(range);
        } else {
            return false;
        }
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