package com.zergatul.scripting.lexer;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String code;
    private final List<DiagnosticMessage> diagnostics;
    private final List<Token> list;
    private int position;
    private int previous;
    private int current;
    private int next;
    private int line;
    private int column;
    private int newLines;
    private int lastNewLineChar;
    private int beginPosition;
    private int beginLine;
    private int beginColumn;

    public Lexer(LexerInput input) {
        this.code = input.code();
        this.diagnostics = new ArrayList<>();

        list = new ArrayList<>();
        line = 1;
        column = 0;
        position = -1;
        previous = -1;
        current = -1;
        next = charAt(0);
        advance();
    }

    public LexerOutput lex() {
        loop:
        while (true) {
            switch (current) {
                case '(':
                    appendToken(TokenType.LEFT_PARENTHESES);
                    advance();
                    break;
                case ')':
                    appendToken(TokenType.RIGHT_PARENTHESES);
                    advance();
                    break;
                case '[':
                    appendToken(TokenType.LEFT_SQUARE_BRACKET);
                    advance();
                    break;
                case ']':
                    appendToken(TokenType.RIGHT_SQUARE_BRACKET);
                    advance();
                    break;
                case '{':
                    appendToken(TokenType.LEFT_CURLY_BRACKET);
                    advance();
                    break;
                case '}':
                    appendToken(TokenType.RIGHT_CURLY_BRACKET);
                    advance();
                    break;
                case '?':
                    appendToken(TokenType.QUESTION);
                    advance();
                    break;
                case '.':
                    appendToken(TokenType.DOT);
                    advance();
                    break;
                case ',':
                    appendToken(TokenType.COMMA);
                    advance();
                    break;
                case ':':
                    appendToken(TokenType.COLON);
                    advance();
                    break;
                case ';':
                    appendToken(TokenType.SEMICOLON);
                    advance();
                    break;
                case '+':
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PLUS_EQUAL);
                    } else {
                        appendToken(TokenType.PLUS);
                        advance();
                    }
                    break;
                case '-':
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.MINUS_EQUAL);
                    } else {
                        appendToken(TokenType.MINUS);
                        advance();
                    }
                    break;
                case '*':
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.ASTERISK_EQUAL);
                    } else {
                        appendToken(TokenType.ASTERISK);
                        advance();
                    }
                    break;
                case '/':
                    switch (next) {
                        case '/':
                            trackBeginToken();
                            advance();
                            advance();
                            while (current != '\r' && current != '\n' && current != -1) {
                                advance();
                            }

                            if (current == '\r' && next == '\n') {
                                advance();
                            }

                            advance();
                            endToken(TokenType.WHITESPACE);
                            break;

                        case '*':
                            trackBeginToken();
                            advance();
                            advance();
                            while (!(current == '*' && next == '/') && current != -1) {
                                advance();
                            }

                            if (current == '*') {
                                advance();
                                advance();
                            }

                            endToken(TokenType.WHITESPACE);
                            break;

                        case '=':
                            trackBeginToken();
                            advance();
                            advance();
                            endToken(TokenType.SLASH_EQUAL);
                            break;

                        default:
                            appendToken(TokenType.SLASH);
                            advance();
                            break;
                    }
                    break;
                case '=':
                    appendToken(TokenType.EQUAL);
                    advance();
                    break;
                case -1:
                    break loop;
                default:
                    if (isWhiteSpace(current)) {
                        trackBeginToken();
                        advance();
                        while (isWhiteSpace(current)) {
                            advance();
                        }
                        endToken(TokenType.WHITESPACE);
                    } else if (isIdentifierStart(current)) {
                        trackBeginToken();
                        advance();
                        while (isIdentifier(current)) {
                            advance();
                        }
                        processIdentifierLike();
                    } else if (isNumber(current)) {
                        trackBeginToken();
                        advance();
                        while (isNumber(current)) {
                            advance();
                        }
                        String value = code.substring(beginPosition, position);
                        list.add(new IntegerToken(value, new SingleLineTextRange(line, beginColumn, beginPosition, position - beginPosition)));
                    } else {
                        Token token = new Token(TokenType.INVALID, new SingleLineTextRange(line, column, position, 1));
                        diagnostics.add(new DiagnosticMessage(LexerErrors.UnexpectedSymbol, token, hex(current)));
                        advance();
                    }
            }
        }

        return new LexerOutput(code, new TokenQueue(list), diagnostics);
    }

    private void processIdentifierLike() {
        String value = code.substring(beginPosition, position);
        TokenType reservedWord = switch (value) {
            case "boolean" -> TokenType.BOOLEAN;
            case "int" -> TokenType.INT;
            case "float" -> TokenType.FLOAT;
            case "string" -> TokenType.STRING;
            case "false" -> TokenType.FALSE;
            case "true" -> TokenType.TRUE;
            case "new" -> TokenType.NEW;
            default -> null;
        };
        TextRange range = new SingleLineTextRange(line, beginColumn, beginPosition, position - beginPosition);
        if (reservedWord != null) {
            list.add(new Token(reservedWord, range));
        } else {
            list.add(new IdentifierToken(value, range));
        }
    }

    private void appendToken(TokenType type) {
        list.add(new Token(type, new SingleLineTextRange(line, column, position, 1)));
    }

    private void trackBeginToken() {
        beginPosition = position;
        beginLine = line;
        beginColumn = column;
    }

    private void endToken(TokenType type) {
        TextRange range = beginLine == line ?
                new SingleLineTextRange(beginLine, beginColumn, beginPosition, position - beginPosition) :
                new MultiLineTextRange(beginLine, beginColumn, line, column, beginPosition, position - beginPosition);
        list.add(new Token(type, range));
    }

    private void advance() {
        if (position >= 0 && current == -1) {
            return;
        }

        position++;
        previous = current;
        current = next;
        next = charAt(position + 1);

        if (current == '\n' || current == '\r') {
            appendNewLineCharacter(current);
        } else {
            releaseNewLineCharacterBuffer();
        }

        column++;
    }

    private int charAt(int index) {
        return index < code.length() ? code.charAt(index) : -1;
    }

    private void appendNewLineCharacter(int ch) {
        if (ch == '\n') {
            advanceLine();
            lastNewLineChar = 0;
        } else {
            if (lastNewLineChar == '\r') {
                advanceLine();
            } else {
                lastNewLineChar = '\r';
            }
        }
    }

    private void releaseNewLineCharacterBuffer() {
        if (lastNewLineChar != 0) {
            advanceLine();
            lastNewLineChar = 0;
        }

        if (newLines > 0) {
            line += newLines;
            newLines = 0;
            column = 0;
        }
    }

    private void advanceLine() {
        newLines++;
    }

    private boolean isWhiteSpace(int ch) {
        return ch == '\t' || ch == '\n' || ch == '\r' || ch == ' ';
    }

    private boolean isNumber(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isIdentifierStart(int ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ch == '_';
    }

    private boolean isIdentifier(int ch) {
        return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9') || ch == '_';
    }

    private String hex(int value) {
        return String.format("%04X", value);
    }
}