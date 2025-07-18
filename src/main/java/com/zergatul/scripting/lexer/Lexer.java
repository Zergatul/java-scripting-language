package com.zergatul.scripting.lexer;

import com.zergatul.scripting.*;

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
                case '(' -> {
                    appendToken(TokenType.LEFT_PARENTHESES);
                    advance();
                }
                case ')' -> {
                    appendToken(TokenType.RIGHT_PARENTHESES);
                    advance();
                }
                case '[' -> {
                    appendToken(TokenType.LEFT_SQUARE_BRACKET);
                    advance();
                }
                case ']' -> {
                    appendToken(TokenType.RIGHT_SQUARE_BRACKET);
                    advance();
                }
                case '{' -> {
                    appendToken(TokenType.LEFT_CURLY_BRACKET);
                    advance();
                }
                case '}' -> {
                    appendToken(TokenType.RIGHT_CURLY_BRACKET);
                    advance();
                }
                case '?' -> {
                    appendToken(TokenType.QUESTION);
                    advance();
                }
                case '.' -> {
                    if (isNumber(next)) {
                        processNumber();
                    } else {
                        appendToken(TokenType.DOT);
                        advance();
                    }
                }
                case ',' -> {
                    appendToken(TokenType.COMMA);
                    advance();
                }
                case ':' -> {
                    appendToken(TokenType.COLON);
                    advance();
                }
                case ';' -> {
                    appendToken(TokenType.SEMICOLON);
                    advance();
                }
                case '+' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PLUS_EQUAL);
                    } else if (next == '+') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PLUS_PLUS);
                    } else {
                        appendToken(TokenType.PLUS);
                        advance();
                    }
                }
                case '-' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.MINUS_EQUAL);
                    } else if (next == '-') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.MINUS_MINUS);
                    } else {
                        appendToken(TokenType.MINUS);
                        advance();
                    }
                }
                case '*' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.ASTERISK_EQUAL);
                    } else {
                        appendToken(TokenType.ASTERISK);
                        advance();
                    }
                }
                case '/' -> {
                    switch (next) {
                        case '/' -> {
                            trackBeginToken();
                            advance();
                            advance();
                            while (current != '\r' && current != '\n' && current != -1) {
                                advance();
                            }

                            endComment(true);
                        }
                        case '*' -> {
                            trackBeginToken();
                            advance();
                            advance();
                            while (true) {
                                if (current == -1) {
                                    endComment(true);
                                    break;
                                } else if (current == '*' && next == '/') {
                                    advance();
                                    advance();
                                    endComment(false);
                                    break;
                                } else if (current == '\n') {
                                    endComment(true);
                                    appendToken(TokenType.LINE_BREAK);
                                    advance();
                                    newLine();
                                    trackBeginToken();
                                } else if (current == '\r') {
                                    endComment(true);
                                    if (next == '\n') {
                                        trackBeginToken();
                                        advance();
                                        advance();
                                        endToken(TokenType.LINE_BREAK);
                                    } else {
                                        appendToken(TokenType.LINE_BREAK);
                                        advance();
                                    }
                                    newLine();
                                    trackBeginToken();
                                } else {
                                    advance();
                                }
                            }
                        }
                        case '=' -> {
                            trackBeginToken();
                            advance();
                            advance();
                            endToken(TokenType.SLASH_EQUAL);
                        }
                        default -> {
                            appendToken(TokenType.SLASH);
                            advance();
                        }
                    }
                }
                case '%' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PERCENT_EQUAL);
                    } else {
                        appendToken(TokenType.PERCENT);
                        advance();
                    }
                }
                case '=' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.EQUAL_EQUAL);
                    } else if (next == '>') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.EQUAL_GREATER);
                    } else {
                        appendToken(TokenType.EQUAL);
                        advance();
                    }
                }
                case '!' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.EXCLAMATION_EQUAL);
                    } else {
                        appendToken(TokenType.EXCLAMATION);
                        advance();
                    }
                }
                case '&' -> {
                    if (next == '&') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.AMPERSAND_AMPERSAND);
                    } else if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.AMPERSAND_EQUAL);
                    } else {
                        appendToken(TokenType.AMPERSAND);
                        advance();
                    }
                }
                case '|' -> {
                    if (next == '|') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PIPE_PIPE);
                    } else if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PIPE_EQUAL);
                    } else {
                        appendToken(TokenType.PIPE);
                        advance();
                    }
                }
                case '<' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.LESS_EQUAL);
                    } else {
                        appendToken(TokenType.LESS);
                        advance();
                    }
                }
                case '>' -> {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.GREATER_EQUAL);
                    } else {
                        appendToken(TokenType.GREATER);
                        advance();
                    }
                }
                case '"' -> {
                    trackBeginToken();
                    while (true) {
                        advance();
                        if (current == -1) {
                            Token token = new StringToken(getCurrentTokenValue(), getCurrentTokenRange());
                            list.add(token);
                            addDiagnostic(LexerErrors.UnfinishedString, token);
                            break;
                        }
                        if (current == '\r' || current == '\n') {
                            Token token = new StringToken(getCurrentTokenValue(), getCurrentTokenRange());
                            list.add(token);
                            addDiagnostic(LexerErrors.NewlineInString, token);
                            break;
                        }
                        if (previous != '\\' && current == '"') {
                            advance();
                            list.add(new StringToken(getCurrentTokenValue(), getCurrentTokenRange()));
                            break;
                        }
                    }
                }
                case '\'' -> {
                    trackBeginToken();
                    while (true) {
                        advance();
                        if (current == '\r' || current == '\n') {
                            Token token = new CharToken(getCurrentTokenValue(), getCurrentTokenRange());
                            list.add(token);
                            addDiagnostic(LexerErrors.NewlineInCharacter, token);
                            break;
                        }
                        if (previous != '\\' && current == '\'') {
                            advance();
                            list.add(new CharToken(getCurrentTokenValue(), getCurrentTokenRange()));
                            break;
                        }
                    }
                }
                case '#' -> {
                    trackBeginToken();
                    advance();
                    while (isIdentifier(current)) {
                        advance();
                    }
                    String value = getCurrentTokenValue();
                    switch (value) {
                        case "#type" -> list.add(new Token(TokenType.META_TYPE, getCurrentTokenRange()));
                        case "#typeof" -> list.add(new Token(TokenType.META_TYPE_OF, getCurrentTokenRange()));
                        default -> {
                            Token token = new Token(TokenType.META_UNKNOWN, getCurrentTokenRange());
                            list.add(token);
                            addDiagnostic(LexerErrors.UnknownMetaFunction, token, value.substring(1));
                        }
                    }
                }
                case '\n' -> {
                    appendToken(TokenType.LINE_BREAK);
                    advance();
                    newLine();
                }
                case '\r' -> {
                    if (next == '\n') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.LINE_BREAK);
                    } else {
                        appendToken(TokenType.LINE_BREAK);
                        advance();
                    }
                    newLine();
                }
                case -1 -> {
                    break loop;
                }
                default -> {
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
                        processNumber();
                    } else {
                        Token token = new Token(TokenType.INVALID, new SingleLineTextRange(line, column, position, 1));
                        addDiagnostic(LexerErrors.UnexpectedSymbol, token, hex(current));
                        advance();
                    }
                }
            }
        }

        return new LexerOutput(code, new TokenQueue(list), diagnostics);
    }

    private void processNumber() {
        if (current == '0' && next == 'x') {
            processHexInteger();
            return;
        }

        trackBeginToken();

        NumberParseState state = NumberParseState.MANTIS_INTEGER;
        int mantisIntegers = 0;
        boolean hasDecimalPoint = false;
        int mantisDecimals = 0;
        boolean hasExponent = false;
        int exponentDigits = 0;

        loop:
        while (true) {
            switch (state) {
                case MANTIS_INTEGER -> {
                    if (isNumber(current)) {
                        mantisIntegers++;
                        advance();
                    } else if (current == '.') {
                        hasDecimalPoint = true;
                        state = NumberParseState.MANTIS_DECIMALS;
                        advance();
                    } else if (current == 'e' || current == 'E') {
                        hasExponent = true;
                        state = NumberParseState.EXPONENT_SIGN;
                        advance();
                    } else {
                        break loop;
                    }
                }
                case MANTIS_DECIMALS -> {
                    if (isNumber(current)) {
                        mantisDecimals++;
                        advance();
                    } else if (current == 'e' || current == 'E') {
                        hasExponent = true;
                        state = NumberParseState.EXPONENT_SIGN;
                        advance();
                    } else {
                        break loop;
                    }
                }
                case EXPONENT_SIGN -> {
                    if (current == '-' || current == '+') {
                        state = NumberParseState.EXPONENT;
                        advance();
                    } else if (isNumber(current)) {
                        state = NumberParseState.EXPONENT;
                    } else {
                        break loop;
                    }
                }
                case EXPONENT -> {
                    if (isNumber(current)) {
                        exponentDigits++;
                        advance();
                    } else {
                        break loop;
                    }
                }
            }
        }

        boolean isValid = (mantisIntegers + mantisDecimals) > 0 && (!hasExponent || exponentDigits > 0);
        boolean isInteger = !hasDecimalPoint && !hasExponent;

        boolean isLong = false;
        if (isInteger && (current == 'L' || current == 'l')) {
            isLong = true;
            advance();
        }

        // check for improper chars after number
        while (current == '.' || isIdentifier(current)) {
            isValid = false;
            advance();
        }

        String value = getCurrentTokenValue();
        TextRange range = getCurrentTokenRange();
        if (isValid) {
            if (isInteger) {
                if (isLong) {
                    list.add(new Integer64Token(value, range));
                } else {
                    list.add(new IntegerToken(value, range));
                }
            } else {
                list.add(new FloatToken(value, range));
            }
        } else {
            Token token = new InvalidNumberToken(value, range);
            addDiagnostic(LexerErrors.InvalidNumber, token, value);
            list.add(token);
        }
    }

    private void processHexInteger() {
        trackBeginToken();

        // skip 0x
        advance();
        advance();

        while (isIdentifier(current)) {
            advance();
        }

        boolean isLong = false;
        String value = getCurrentTokenValue();
        if (value.endsWith("L") || value.endsWith("l")) {
            isLong = true;
        }

        TextRange range = getCurrentTokenRange();
        boolean isValidHex = isLong ?
                value.chars().skip(2).limit(value.length() - 3).allMatch(this::isHexNumber) :
                value.chars().skip(2).allMatch(this::isHexNumber);
        if (value.length() == 2 || !isValidHex) {
            Token token = new InvalidNumberToken(value, range);
            addDiagnostic(LexerErrors.InvalidNumber, token, value);
            list.add(token);
        } else {
            if (isLong) {
                list.add(new Integer64Token(value, range));
            } else {
                list.add(new IntegerToken(value, range));
            }
        }
    }

    private void processIdentifierLike() {
        String value = getCurrentTokenValue();
        TokenType reservedWord = switch (value) {
            case "boolean" -> TokenType.BOOLEAN;
            case "int" -> TokenType.INT;
            case "int32" -> TokenType.INT32;
            case "int64" -> TokenType.INT64;
            case "long" -> TokenType.LONG;
            case "float" -> TokenType.FLOAT;
            case "string" -> TokenType.STRING;
            case "char" -> TokenType.CHAR;
            case "false" -> TokenType.FALSE;
            case "true" -> TokenType.TRUE;
            case "new" -> TokenType.NEW;
            case "if" -> TokenType.IF;
            case "else" -> TokenType.ELSE;
            case "return" -> TokenType.RETURN;
            case "for" -> TokenType.FOR;
            case "foreach" -> TokenType.FOREACH;
            case "while" -> TokenType.WHILE;
            case "break" -> TokenType.BREAK;
            case "continue" -> TokenType.CONTINUE;
            case "in" -> TokenType.IN;
            case "static" -> TokenType.STATIC;
            case "void" -> TokenType.VOID;
            case "ref" -> TokenType.REF;
            case "async" -> TokenType.ASYNC;
            case "await" -> TokenType.AWAIT;
            case "let" -> TokenType.LET;
            case "is" -> TokenType.IS;
            case "as" -> TokenType.AS;
            default -> null;
        };
        TextRange range = getCurrentTokenRange();
        if (reservedWord != null) {
            list.add(new Token(reservedWord, range));
        } else {
            list.add(new IdentifierToken(value, range));
        }
    }

    private String getCurrentTokenValue() {
        return code.substring(beginPosition, position);
    }

    private SingleLineTextRange getCurrentTokenRange() {
        return new SingleLineTextRange(line, beginColumn, beginPosition, position - beginPosition);
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
        if (position - beginPosition == 0) {
            return;
        }
        TextRange range = beginLine == line ?
                new SingleLineTextRange(beginLine, beginColumn, beginPosition, position - beginPosition) :
                new MultiLineTextRange(beginLine, beginColumn, line, column, beginPosition, position - beginPosition);
        list.add(new Token(type, range));
    }

    private void endComment(boolean ending) {
        if (beginLine != line) {
            throw new InternalException();
        }
        list.add(new CommentToken(ending, new SingleLineTextRange(beginLine, beginColumn, beginPosition, position - beginPosition)));
    }

    private void advance() {
        if (position >= 0 && current == -1) {
            return;
        }

        position++;
        previous = current;
        current = next;
        next = charAt(position + 1);

        column++;
    }

    private void newLine() {
        line++;
        column = 1;
    }

    private int charAt(int index) {
        return index < code.length() ? code.charAt(index) : -1;
    }

    private void addDiagnostic(ErrorCode code, Token token, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, token, parameters));
    }

    private boolean isWhiteSpace(int ch) {
        return ch == '\t' || ch == ' ';
    }

    private boolean isNumber(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private boolean isHexNumber(int ch) {
        return ('0' <= ch && ch <= '9') || ('a' <= ch && ch <= 'f') || ('A' <= ch && ch <= 'F');
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

    private enum NumberParseState {
        MANTIS_INTEGER,
        MANTIS_DECIMALS,
        EXPONENT_SIGN,
        EXPONENT
    }
}