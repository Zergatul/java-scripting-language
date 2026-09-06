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
                case '(': {
                    appendToken(TokenType.LEFT_PARENTHESES);
                    advance();
                    break;
                }
                case ')': {
                    appendToken(TokenType.RIGHT_PARENTHESES);
                    advance();
                    break;
                }
                case '[': {
                    appendToken(TokenType.LEFT_SQUARE_BRACKET);
                    advance();
                    break;
                }
                case ']': {
                    appendToken(TokenType.RIGHT_SQUARE_BRACKET);
                    advance();
                    break;
                }
                case '{': {
                    appendToken(TokenType.LEFT_CURLY_BRACKET);
                    advance();
                    break;
                }
                case '}': {
                    appendToken(TokenType.RIGHT_CURLY_BRACKET);
                    advance();
                    break;
                }
                case '?': {
                    if (next == '?') {
                        trackBeginToken();
                        advance();
                        advance();
                        if (current == '=') {
                            advance();
                            endToken(TokenType.QUESTION_QUESTION_EQUAL);
                        } else {
                            endToken(TokenType.QUESTION_QUESTION);
                        }
                    } else {
                        appendToken(TokenType.QUESTION);
                        advance();
                    }
                    break;
                }
                case '.': {
                    if (next == '#') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.DOT_HASH);
                    } else if (isNumber(next)) {
                        processNumber();
                    } else {
                        appendToken(TokenType.DOT);
                        advance();
                    }
                    break;
                }
                case '$': {
                    appendToken(TokenType.DOLLAR);
                    advance();
                    break;
                }
                case ',': {
                    appendToken(TokenType.COMMA);
                    advance();
                    break;
                }
                case ':': {
                    appendToken(TokenType.COLON);
                    advance();
                    break;
                }
                case ';': {
                    appendToken(TokenType.SEMICOLON);
                    advance();
                    break;
                }
                case '+': {
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
                    break;
                }
                case '-': {
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
                    break;
                }
                case '*': {
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
                }
                case '/': {
                    switch (next) {
                        case '/':
                            trackBeginToken();
                            advance();
                            advance();
                            while (current != '\r' && current != '\n' && current != -1) {
                                advance();
                            }

                            endComment(TokenType.SINGLE_LINE_COMMENT);
                            break;
                        case '*':
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
                }
                case '%': {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.PERCENT_EQUAL);
                    } else {
                        appendToken(TokenType.PERCENT);
                        advance();
                    }
                    break;
                }
                case '=': {
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
                    break;
                }
                case '!': {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.EXCLAMATION_EQUAL);
                    } else {
                        appendToken(TokenType.EXCLAMATION);
                        advance();
                    }
                    break;
                }
                case '&': {
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
                    break;
                }
                case '|': {
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
                    break;
                }
                case '<': {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.LESS_EQUAL);
                    } else {
                        appendToken(TokenType.LESS);
                        advance();
                    }
                    break;
                }
                case '>': {
                    if (next == '=') {
                        trackBeginToken();
                        advance();
                        advance();
                        endToken(TokenType.GREATER_EQUAL);
                    } else {
                        appendToken(TokenType.GREATER);
                        advance();
                    }
                    break;
                }
                case '"': {
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
                    break;
                }
                case '\'': {
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
                    break;
                }
                case '#': {
                    trackBeginToken();
                    advance();
                    while (isIdentifier(current)) {
                        advance();
                    }
                    String value = getCurrentTokenValue();
                    switch (value) {
                        case "#cast":
                            appendToken(new Token(TokenType.META_CAST, getCurrentTokenRange()));
                            break;
                        case "#type":
                            appendToken(new Token(TokenType.META_TYPE, getCurrentTokenRange()));
                            break;
                        case "#typeof":
                            appendToken(new Token(TokenType.META_TYPE_OF, getCurrentTokenRange()));
                            break;
                        default:
                            Token token = new Token(TokenType.META_UNKNOWN, getCurrentTokenRange());
                            appendToken(token);
                            addDiagnostic(LexerErrors.UnknownMetaFunction, token, value.substring(1));
                            break;
                    }
                    break;
                }
                case '\n': {
                    trackBeginToken();
                    advance();
                    newLine(1);
                    endToken(TokenType.LINE_BREAK);
                    break;
                }
                case '\r': {
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
                    break;
                }
                case -1: {
                    break loop;
                }
                default: {
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
                    break;
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
                case MANTIS_INTEGER:
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
                    break;
                case MANTIS_DECIMALS:
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
                    break;
                case EXPONENT_SIGN:
                    if (current == '-' || current == '+') {
                        state = NumberParseState.EXPONENT;
                        advance();
                    } else if (isNumber(current)) {
                        state = NumberParseState.EXPONENT;
                    } else {
                        break loop;
                    }
                    break;
                case EXPONENT:
                    if (isNumber(current)) {
                        exponentDigits++;
                        advance();
                    } else {
                        break loop;
                    }
                    break;
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
        TokenType reservedWord;
        switch (value) {
            case "boolean":
                reservedWord = TokenType.BOOLEAN;
                break;
            case "int8":
                reservedWord = TokenType.INT8;
                break;
            case "int16":
                reservedWord = TokenType.INT16;
                break;
            case "int":
                reservedWord = TokenType.INT;
                break;
            case "int32":
                reservedWord = TokenType.INT32;
                break;
            case "int64":
                reservedWord = TokenType.INT64;
                break;
            case "long":
                reservedWord = TokenType.LONG;
                break;
            case "float32":
                reservedWord = TokenType.FLOAT32;
                break;
            case "float":
                reservedWord = TokenType.FLOAT;
                break;
            case "float64":
                reservedWord = TokenType.FLOAT64;
                break;
            case "string":
                reservedWord = TokenType.STRING;
                break;
            case "char":
                reservedWord = TokenType.CHAR;
                break;
            case "false":
                reservedWord = TokenType.FALSE;
                break;
            case "true":
                reservedWord = TokenType.TRUE;
                break;
            case "new":
                reservedWord = TokenType.NEW;
                break;
            case "if":
                reservedWord = TokenType.IF;
                break;
            case "else":
                reservedWord = TokenType.ELSE;
                break;
            case "return":
                reservedWord = TokenType.RETURN;
                break;
            case "for":
                reservedWord = TokenType.FOR;
                break;
            case "foreach":
                reservedWord = TokenType.FOREACH;
                break;
            case "while":
                reservedWord = TokenType.WHILE;
                break;
            case "break":
                reservedWord = TokenType.BREAK;
                break;
            case "continue":
                reservedWord = TokenType.CONTINUE;
                break;
            case "in":
                reservedWord = TokenType.IN;
                break;
            case "static":
                reservedWord = TokenType.STATIC;
                break;
            case "void":
                reservedWord = TokenType.VOID;
                break;
            case "ref":
                reservedWord = TokenType.REF;
                break;
            case "async":
                reservedWord = TokenType.ASYNC;
                break;
            case "await":
                reservedWord = TokenType.AWAIT;
                break;
            case "let":
                reservedWord = TokenType.LET;
                break;
            case "is":
                reservedWord = TokenType.IS;
                break;
            case "as":
                reservedWord = TokenType.AS;
                break;
            case "class":
                reservedWord = TokenType.CLASS;
                break;
            case "constructor":
                reservedWord = TokenType.CONSTRUCTOR;
                break;
            case "this":
                reservedWord = TokenType.THIS;
                break;
            case "base":
                reservedWord = TokenType.BASE;
                break;
            case "extension":
                reservedWord = TokenType.EXTENSION;
                break;
            case "abstract":
                reservedWord = TokenType.ABSTRACT;
                break;
            case "virtual":
                reservedWord = TokenType.VIRTUAL;
                break;
            case "override":
                reservedWord = TokenType.OVERRIDE;
                break;
            case "public":
                reservedWord = TokenType.PUBLIC;
                break;
            case "protected":
                reservedWord = TokenType.PROTECTED;
                break;
            case "private":
                reservedWord = TokenType.PRIVATE;
                break;
            case "typealias":
                reservedWord = TokenType.TYPEALIAS;
                break;
            case "null":
                reservedWord = TokenType.NULL;
                break;
            case "try":
                reservedWord = TokenType.TRY;
                break;
            case "catch":
                reservedWord = TokenType.CATCH;
                break;
            case "finally":
                reservedWord = TokenType.FINALLY;
                break;
            case "throw":
                reservedWord = TokenType.THROW;
                break;
            default:
                reservedWord = null;
                break;
        }

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
        switch (current) {
            case 'n':
                return '\n';
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'r':
                return '\r';
            case 'f':
                return '\f';
            case '\'':
                return '\'';
            case '\"':
                return '\"';
            case '\\':
                return '\\';

            case 'u':
                StringBuilder builder = new StringBuilder(4);
                while (builder.length() < 4) {
                    if (isHexNumber(next)) {
                        advance();
                        builder.append((char) current);
                    } else {
                        diagnostics.add(
                                new DiagnosticMessage(LexerErrors.InvalidEscapeSequence,
                                        new SingleLineTextRange(beginLine, beginColumn, beginPosition, 2 + builder.length())));
                        return (char) 0;
                    }
                }
                return (char) Integer.parseInt(builder.toString(), 16);

            case -1:
                return 0;

            default:
                diagnostics.add(
                        new DiagnosticMessage(LexerErrors.InvalidEscapeSequence,
                                new SingleLineTextRange(beginLine, beginColumn, beginPosition, 2)));
                return (char)current;
        }
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

        Token last = list.get(list.size() - 1);
        List<Trivia> trailing = last.getTrailingTrivia();
        if (trailing.isEmpty()) {
            list.set(list.size() - 1, last.withTrailingTrivia(trivia));
            return;
        }

        Trivia lastTrivia = trailing.get(trailing.size() - 1);
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
            int lastEndPos = lines.get(lines.size() - 1).endPosition();
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