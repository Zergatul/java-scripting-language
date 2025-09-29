package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.EndOfFileToken;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.lexer.ValueToken;

public abstract class Dumper {

    protected final StringBuilder sb = new StringBuilder();
    private int indent;

    protected void reset() {
        indent = 0;
        sb.setLength(0);
    }

    protected void dump(Token token) {
        if (token == null) {
            fullLine("null");
        } else if (token.getClass() == Token.class) {
            dumpToken(token);
        } else if (token.getClass() == ValueToken.class) {
            dumpValueToken((ValueToken) token);
        } else if (token.getClass() == EndOfFileToken.class) {
            dumpEndOfFileToken((EndOfFileToken) token);
        } else {
            throw new InternalException();
        }
    }

    protected void dumpToken(Token token) {
        beginLine("new Token(TokenType.");
        sb.append(token.getTokenType());
        sb.append(", ");
        dump(token.getRange());
        sb.append(")");

        incIndent();
        for (Trivia trivia : token.getLeadingTrivia()) {
            sb.append("\n");
            beginLine(".withLeadingTrivia(");
            dump(trivia);
            sb.append(")");
        }

        for (Trivia trivia : token.getTrailingTrivia()) {
            sb.append("\n");
            beginLine(".withTrailingTrivia(");
            dump(trivia);
            sb.append(")");
        }
        decIndent();
    }

    protected void dumpValueToken(ValueToken token) {
        beginLine("new ValueToken(TokenType.");
        sb.append(token.getTokenType());
        sb.append(", \"");
        for (char ch : token.value.toCharArray()) {
            switch (ch) {
                case '\r' -> sb.append("\\r");
                case '\n' -> sb.append("\\n");
                case '"' -> sb.append("\"");
                case '\\' -> sb.append("\\\\");
                default -> sb.append(ch);
            }
        }
        sb.append("\", ");
        dump(token.getRange());
        sb.append(")");

        incIndent();
        for (Trivia trivia : token.getLeadingTrivia()) {
            sb.append("\n");
            beginLine(".withLeadingTrivia(");
            dump(trivia);
            sb.append(")");
        }

        for (Trivia trivia : token.getTrailingTrivia()) {
            sb.append("\n");
            beginLine(".withTrailingTrivia(");
            dump(trivia);
            sb.append(")");
        }
        decIndent();
    }

    protected void dump(Trivia trivia) {
        sb.append("new Trivia(TokenType.");
        sb.append(trivia.getTokenType());
        sb.append(", ");
        dump(trivia.getRange());
        sb.append(")");
    }

    protected void dumpEndOfFileToken(EndOfFileToken token) {
        beginLine("new EndOfFileToken(");
        dump(token.getRange());
        sb.append(")");

        incIndent();
        for (Trivia trivia : token.getLeadingTrivia()) {
            sb.append("\n");
            beginLine(".withLeadingTrivia(");
            dump(trivia);
            sb.append(")");
        }
        decIndent();
    }

    protected void dump(TextRange range) {
        beginNewLineIfRequired();

        if (range instanceof SingleLineTextRange single) {
            sb.append("new SingleLineTextRange(");
            sb.append(single.getLine1());
            sb.append(", ");
            sb.append(single.getColumn1());
            sb.append(", ");
            sb.append(single.getPosition());
            sb.append(", ");
            sb.append(single.getLength());
            sb.append(")");
        } else if (range instanceof MultiLineTextRange multi) {
            sb.append("new MultiLineTextRange(");
            sb.append(multi.getLine1());
            sb.append(", ");
            sb.append(multi.getColumn1());
            sb.append(", ");
            sb.append(multi.getLine2());
            sb.append(", ");
            sb.append(multi.getColumn2());
            sb.append(", ");
            sb.append(multi.getPosition());
            sb.append(", ");
            sb.append(multi.getLength());
            sb.append(")");
        } else {
            throw new InternalException();
        }
    }

    protected void beginLine() {
        beginLine("");
    }

    protected void beginLine(String value) {
        sb.append(" ".repeat(indent));
        sb.append(value);
    }

    protected void endLine(String value) {
        sb.append(value);
        sb.append('\n');
    }

    protected void fullLine(String value) {
        sb.append(" ".repeat(indent));
        sb.append(value);
        sb.append('\n');
    }

    protected void commaBreak() {
        sb.append(",\n");
    }

    protected boolean beginNewLineIfRequired() {
        if (isNewLine()) {
            beginLine();
            return true;
        }
        return false;
    }

    protected boolean isNewLine() {
        return sb.charAt(sb.length() - 1) == '\n';
    }

    protected void incIndent() {
        indent += 8;
    }

    protected void decIndent() {
        indent -= 8;
    }
}