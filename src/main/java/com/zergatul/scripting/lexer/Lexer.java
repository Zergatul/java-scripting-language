package com.zergatul.scripting.lexer;

import com.zergatul.scripting.*;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String code;
    private final List<Line> lines;
    private final List<DiagnosticMessage> diagnostics;
    private final List<Token> list;
    private final List<Trivia> triviaBuffer;
    private int position;
    private int current;
    private int next;
    private int line;
    private int column;
    private int beginPosition;
    private int beginLine;
    private int beginColumn;

    public Lexer(LexerInput input) {
        this.code = input.code();
        this.lines = new ArrayList<>();
        this.diagnostics = new ArrayList<>();

        this.list = new ArrayList<>();
        this.triviaBuffer = new ArrayList<>();
        this.line = 1;
        this.column = 0;
        this.position = -1;
        this.current = -1;
        this.next = charAt(0);
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
                case '$' -> {
                    appendToken(TokenType.DOLLAR);
                    advance();
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

                            endComment(TokenType.SINGLE_LINE_COMMENT);
                        }
                        case '*' -> {
                            trackBeginToken();
                            advance();
                            advance();
                            while (true) {
                                if (current == -1) {
                                    endComment(TokenType.MULTI_LINE_COMMENT);
                                    break;
                                } else if (current == '*' && next == '/') {
                                    advance();
                                    advance();
                                    endComment(TokenType.MULTI_LINE_COMMENT);
                                    break;
                                } else if (current == '\n') {
                                    advance();
                                    newLine(1);
                                } else if (current == '\r') {
                                    if (next == '\n') {
                                        advance();
                                        advance();
                                        newLine(2);
                                    } else {
                                        advance();
                                        newLine(1);
                                    }
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
                    StringBuilder builder = new StringBuilder();
                    while (true) {
                        advance();
                        if (current == -1) {
                            Token token = new ValueToken(TokenType.STRING_LITERAL, builder.toString(), getCurrentTokenRange());
                            appendToken(token);
                            addDiagnostic(LexerErrors.UnfinishedString, token);
                            break;
                        } else if (current == '\\') {
                            builder.append(processEscapedChar());
                        } else if (current == '\r' || current == '\n') {
                            Token token = new ValueToken(TokenType.STRING_LITERAL, builder.toString(), getCurrentTokenRange());
                            appendToken(token);
                            addDiagnostic(LexerErrors.NewlineInString, token);
                            break;
                        } else if (current == '"') {
                            advance();
                            appendToken(new ValueToken(TokenType.STRING_LITERAL, builder.toString(), getCurrentTokenRange()));
                            break;
                        } else {
                            builder.append((char) current);
                        }
                    }
                }
                case '\'' -> {
                    trackBeginToken();
                    char value = (char) 0;
                    boolean hasValue = false;
                    boolean tooMany = false;
                    while (true) {
                        advance();
                        if (current == -1 || current == '\r' || current == '\n') {
                            Token token = new ValueToken(TokenType.CHAR_LITERAL, String.valueOf(value), getCurrentTokenRange());
                            appendToken(token);
                            addDiagnostic(LexerErrors.NewlineInCharacter, token);
                            break;
                        } else if (current == '\\') {
                            tooMany = hasValue;
                            value = processEscapedChar();
                            hasValue = true;
                        } else if (current == '\'') {
                            advance();
                            Token token = new ValueToken(TokenType.CHAR_LITERAL, String.valueOf(value), getCurrentTokenRange());
                            appendToken(token);
                            if (token.getRange().getLength() <= 2) {
                                addDiagnostic(LexerErrors.EmptyCharacterLiteral, token);
                            }
                            if (tooMany) {
                                addDiagnostic(LexerErrors.TooManyCharsInCharLiteral, token);
                            }
                            break;
                        } else {
                            tooMany = hasValue;
                            value = (char) current;
                            hasValue = true;
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
                        case "#type" -> appendToken(new Token(TokenType.META_TYPE, getCurrentTokenRange()));
                        case "#typeof" -> appendToken(new Token(TokenType.META_TYPE_OF, getCurrentTokenRange()));
                        default -> {
                            Token token = new Token(TokenType.META_UNKNOWN, getCurrentTokenRange());
                            appendToken(token);
                            addDiagnostic(LexerErrors.UnknownMetaFunction, token, value.substring(1));
                        }
                    }
                }
                case '\n' -> {
                    trackBeginToken();
                    advance();
                    newLine(1);
                    endToken(TokenType.LINE_BREAK);
                }
                case '\r' -> {
                    trackBeginToken();
                    if (next == '\n') {
                        advance();
                        advance();
                        newLine(2);
                    } else {
                        advance();
                        newLine(1);
                    }
                    endToken(TokenType.LINE_BREAK);
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
                        appendToken(token);
                        addDiagnostic(LexerErrors.UnexpectedSymbol, token, hex(current));
                        advance();
                    }
                }
            }
        }

        appendToken(new EndOfFileToken(new SingleLineTextRange(line, column, position, 0)));

        return new LexerOutput(code, lines, new TokenQueue(list), diagnostics);
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
                    } else if (current == '.' && isNumber(next)) {
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
        while (isIdentifier(current)) {
            isValid = false;
            advance();
        }

        String value = getCurrentTokenValue();
        TextRange range = getCurrentTokenRange();
        if (isValid) {
            if (isInteger) {
                if (isLong) {
                    appendToken(new ValueToken(TokenType.INTEGER64_LITERAL, value, range));
                } else {
                    appendToken(new ValueToken(TokenType.INTEGER_LITERAL, value, range));
                }
            } else {
                appendToken(new ValueToken(TokenType.FLOAT_LITERAL, value, range));
            }
        } else {
            Token token = new InvalidNumberToken(value, range);
            addDiagnostic(LexerErrors.InvalidNumber, token, value);
            appendToken(token);
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
            appendToken(token);
        } else {
            if (isLong) {
                appendToken(new ValueToken(TokenType.INTEGER64_LITERAL, value, range));
            } else {
                appendToken(new ValueToken(TokenType.INTEGER_LITERAL, value, range));
            }
        }
    }

    private void processIdentifierLike() {
        String value = getCurrentTokenValue();
        TokenType reservedWord = switch (value) {
            case "boolean" -> TokenType.BOOLEAN;
            case "int8" -> TokenType.INT8;
            case "int16" -> TokenType.INT16;
            case "int" -> TokenType.INT;
            case "int32" -> TokenType.INT32;
            case "int64" -> TokenType.INT64;
            case "long" -> TokenType.LONG;
            case "float32" -> TokenType.FLOAT32;
            case "float" -> TokenType.FLOAT;
            case "float64" -> TokenType.FLOAT64;
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
            case "class" -> TokenType.CLASS;
            case "constructor" -> TokenType.CONSTRUCTOR;
            case "this" -> TokenType.THIS;
            case "base" -> TokenType.BASE;
            case "extension" -> TokenType.EXTENSION;
            case "abstract" -> TokenType.ABSTRACT;
            case "virtual" -> TokenType.VIRTUAL;
            case "override" -> TokenType.OVERRIDE;
            case "typealias" -> TokenType.TYPEALIAS;
            default -> null;
        };
        TextRange range = getCurrentTokenRange();
        if (reservedWord != null) {
            appendToken(new Token(reservedWord, range));
        } else {
            appendToken(new ValueToken(TokenType.IDENTIFIER, value, range));
        }
    }

    private char processEscapedChar() {
        int beginLine = line;
        int beginColumn = column;
        int beginPosition = position;
        advance();
        return switch (current) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'b' -> '\b';
            case 'r' -> '\r';
            case 'f' -> '\f';
            case '\'' -> '\'';
            case '\"' -> '\"';
            case '\\' -> '\\';
            case 'u' -> {
                StringBuilder builder = new StringBuilder(4);
                while (builder.length() < 4) {
                    if (isHexNumber(next)) {
                        advance();
                        builder.append((char) current);
                    } else {
                        diagnostics.add(
                                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence,
                                new SingleLineTextRange(beginLine, beginColumn, beginPosition, 2 + builder.length())));
                        yield (char) 0;
                    }
                }
                yield (char) Integer.parseInt(builder.toString(), 16);
            }
            case -1 -> 0;
            default -> {
                diagnostics.add(
                        new DiagnosticMessage(LexerErrors.InvalidEscapeSequence,
                        new SingleLineTextRange(beginLine, beginColumn, beginPosition, 2)));
                yield (char) current;
            }
        };
    }

    private String getCurrentTokenValue() {
        return code.substring(beginPosition, position);
    }

    private SingleLineTextRange getCurrentTokenRange() {
        return new SingleLineTextRange(line, beginColumn, beginPosition, position - beginPosition);
    }

    private void appendToken(TokenType type) {
        appendToken(new Token(type, new SingleLineTextRange(line, column, position, 1)));
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

        if (isTriviaType(type)) {
            appendTrivia(new Trivia(type, range));
        } else {
            appendToken(new Token(type, range));
        }
    }

    private void endComment(TokenType type) {
        TextRange range = beginLine == line ?
                new SingleLineTextRange(beginLine, beginColumn, beginPosition, position - beginPosition) :
                new MultiLineTextRange(beginLine, beginColumn, line, column, beginPosition, position - beginPosition);

        appendTrivia(new Trivia(type, range));
    }

    private void appendToken(Token token) {
        if (triviaBuffer.isEmpty()) {
            list.add(token);
        } else {
            list.add(token.withLeadingTrivia(triviaBuffer));
            triviaBuffer.clear();
        }
    }

    private void appendTrivia(Trivia trivia) {
        if (list.isEmpty()) {
            triviaBuffer.add(trivia);
            return;
        }

        Token last = list.getLast();
        List<Trivia> trailing = last.getTrailingTrivia();
        if (trailing.isEmpty()) {
            list.set(list.size() - 1, last.withTrailingTrivia(trivia));
            return;
        }

        Trivia lastTrivia = trailing.getLast();
        if (lastTrivia.is(TokenType.LINE_BREAK)) {
            triviaBuffer.add(trivia);
        } else {
            list.set(list.size() - 1, last.withTrailingTrivia(trivia));
        }
    }

    private boolean isTriviaType(TokenType type) {
        return type == TokenType.WHITESPACE || type == TokenType.LINE_BREAK || type == TokenType.SINGLE_LINE_COMMENT || type == TokenType.MULTI_LINE_COMMENT;
    }

    private void advance() {
        if (position >= 0 && current == -1) {
            return;
        }

        position++;
        current = next;
        next = charAt(position + 1);

        column++;
    }

    private void newLine(int lineBreakLen) {
        line++;
        column = 1;

        if (lines.isEmpty()) {
            lines.add(new Line(0, position - lineBreakLen, position));
        } else {
            int lastEndPos = lines.getLast().endPosition();
            lines.add(new Line(lastEndPos, position - lastEndPos - lineBreakLen, position));
        }
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