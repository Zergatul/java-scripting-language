package com.zergatul.scripting.parser;

import com.zergatul.scripting.*;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Parser {

    private final String code;
    private final TokenQueue tokens;
    private final List<DiagnosticMessage> diagnostics;
    private Token current;
    private Token last;

    public Parser(LexerOutput input) {
        this.code = input.code();
        this.tokens = input.tokens();
        this.diagnostics = input.diagnostics();
        advance();
    }

    public ParserOutput parse() {
        return new ParserOutput(code, parseCompilationUnit(), diagnostics);
    }

    private CompilationUnitNode parseCompilationUnit() {
        List<CompilationUnitMemberNode> members = new ArrayList<>();
        while (!current.is(TokenType.END_OF_FILE)) {
            if (current.is(TokenType.STATIC)) {
                members.add(parseStaticVariable());
            } else if (current.is(TokenType.CLASS)) {
                members.add(parseClass());
            } else if (isPossibleFunction()) {
                members.add(parseFunction());
            } else {
                break;
            }
        }

        List<StatementNode> statements = new ArrayList<>();
        while (current.isNot(TokenType.END_OF_FILE)) {
            if (isPossibleStatement()) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                advance();
            }
        }

        EndOfFileToken end = (EndOfFileToken) current;

        CompilationUnitMembersListNode membersList = new CompilationUnitMembersListNode(
                members,
                members.isEmpty() ?
                        new SingleLineTextRange(1, 1, 0, 0) :
                        TextRange.combine(members.getFirst(), members.getLast()));

        StatementsListNode statementsList = new StatementsListNode(
                statements,
                statements.isEmpty() ?
                        new SingleLineTextRange(
                                membersList.getRange().getLine2(),
                                membersList.getRange().getColumn2(),
                                membersList.getRange().getPosition() + membersList.getRange().getLength(),
                                0) :
                        TextRange.combine(statements.getFirst(), statements.getLast()));

        return new CompilationUnitNode(membersList, statementsList, end);
    }

    private BlockStatementNode parseBlockStatement() {
        Token openBrace = advance(TokenType.LEFT_CURLY_BRACKET);

        List<StatementNode> statements = new ArrayList<>();
        while (current.isNot(TokenType.RIGHT_CURLY_BRACKET) && current.isNot(TokenType.END_OF_FILE)) {
            if (isPossibleStatement()) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                advance();
            }
        }

        Token closeBrace = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new BlockStatementNode(openBrace, statements, closeBrace, TextRange.combine(openBrace, closeBrace));
    }

    private BlockStatementNode createMissingBlockStatement() {
        TextRange range = createMissingTokenRangeAfterLast();
        return new BlockStatementNode(
                new Token(TokenType.LEFT_CURLY_BRACKET, range),
                List.of(),
                new Token(TokenType.RIGHT_CURLY_BRACKET, range),
                range);
    }

    private IfStatementNode parseIfStatement() {
        Token ifToken = advance(TokenType.IF);

        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        if (openParen.getRange().isEmpty()) {
            // assume entire statement is missing
            return new IfStatementNode(
                    ifToken,
                    openParen,
                    new InvalidExpressionNode(createMissingTokenRange()),
                    createMissingToken(TokenType.RIGHT_PARENTHESES),
                    new InvalidStatementNode(createMissingTokenRange()),
                    null,
                    null,
                    ifToken.getRange());
        }

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRange());
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        StatementNode thenStatement;
        if (isPossibleStatement()) {
            thenStatement = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            thenStatement = new InvalidStatementNode(createMissingTokenRangeAfterLast());
        }

        Token elseToken = null;
        StatementNode elseStatement = null;
        if (current.is(TokenType.ELSE)) {
            elseToken = advance();
            if (isPossibleStatement()) {
                elseStatement = parseStatement();
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                elseStatement = new InvalidStatementNode(createMissingTokenRange());
            }
        }

        return new IfStatementNode(
                ifToken,
                openParen,
                condition,
                closeParen,
                thenStatement,
                elseToken,
                elseStatement,
                TextRange.combine(ifToken, elseStatement == null ? thenStatement : elseStatement));
    }

    private ReturnStatementNode parseReturnStatement() {
        Token keyword = advance(TokenType.RETURN);
        ExpressionNode expression = null;
        if (isPossibleExpression()) {
            expression = parseExpression();
        }

        Token semicolon = advance(TokenType.SEMICOLON);
        return new ReturnStatementNode(keyword, expression, TextRange.combine(keyword, semicolon));
    }

    private ForLoopStatementNode parseForLoopStatement() {
        Token keyword = advance(TokenType.FOR);
        Token lParen = advance(TokenType.LEFT_PARENTHESES);

        if (lParen.getRange().isEmpty()) {
            // assume entire statement is missing
            return new ForLoopStatementNode(
                    keyword,
                    lParen,
                    createMissingInvalidStatement(),
                    new InvalidExpressionNode(createMissingTokenRangeAfterLast()),
                    createMissingInvalidStatement(),
                    createMissingTokenAfterLast(TokenType.RIGHT_PARENTHESES),
                    createMissingInvalidStatement());
        }

        StatementNode init;
        if (current.is(TokenType.SEMICOLON)) {
            init = new EmptyStatementNode(advance(TokenType.SEMICOLON));
        } else {
            if (isPossibleSimpleStatement()) {
                init = parseSimpleStatement();
            } else {
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
                init = new InvalidStatementNode(createMissingTokenRange());
            }
            advance(TokenType.SEMICOLON);
        }

        ExpressionNode condition = null;
        if (current.is(TokenType.SEMICOLON)) {
            advance();
        } else {
            if (isPossibleExpression()) {
                condition = parseExpression();
            } else {
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            }
            advance(TokenType.SEMICOLON);
        }

        StatementNode update;
        if (current.is(TokenType.RIGHT_PARENTHESES)) {
            update = null;
        } else {
            if (isPossibleSimpleStatement() && !isPossibleDeclaration()) {
                update = parseSimpleStatementNotDeclaration(false);
            } else {
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
                update = new InvalidStatementNode(createMissingTokenRange());
            }
        }

        Token rParen = advance(TokenType.RIGHT_PARENTHESES);
        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRange());
        }

        return new ForLoopStatementNode(keyword, lParen, init, condition, update, rParen, body);
    }

    private ForEachLoopStatementNode parseForEachLoopStatement() {
        Token keyword = advance(TokenType.FOREACH);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);

        if (openParen.getRange().isEmpty()) {
            // assume entire statement is missing
            TextRange range = createMissingTokenRangeAfterLast();
            return new ForEachLoopStatementNode(
                    keyword,
                    openParen,
                    new InvalidTypeNode(range),
                    new NameExpressionNode("", range),
                    new Token(TokenType.IN, range),
                    new InvalidExpressionNode(range),
                    new Token(TokenType.RIGHT_PARENTHESES, range),
                    createMissingInvalidStatement());
        }

        TypeNode typeNode;
        if (isPossibleDeclaration()) {
            typeNode = parseLetTypeNode();
        } else {
            addDiagnostic(ParserErrors.ForEachTypeIdentifierRequired, current);
            typeNode = new InvalidTypeNode(createMissingTokenRange());
        }

        ValueToken identifier;
        if (current.is(TokenType.IDENTIFIER)) {
            identifier = (ValueToken) advance();
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRange());
        }

        Token in = advance(TokenType.IN);

        ExpressionNode iterable;
        if (isPossibleExpression()) {
            iterable = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            iterable = new InvalidExpressionNode(createMissingTokenRange());
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRange());
        }

        return new ForEachLoopStatementNode(
                keyword,
                openParen,
                typeNode,
                new NameExpressionNode(identifier),
                in,
                iterable,
                closeParen,
                body);
    }

    private WhileLoopStatementNode parseWhileLoopStatement() {
        Token keyword = advance(TokenType.WHILE);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);

        if (openParen.isMissing()) {
            // assume entire statement is missing
            return new WhileLoopStatementNode(
                    keyword,
                    openParen,
                    new InvalidExpressionNode(createMissingTokenRange()),
                    new Token(TokenType.RIGHT_PARENTHESES, createMissingTokenRange()),
                    new InvalidStatementNode(createMissingTokenRange()),
                    keyword.getRange());
        }

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRange());
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRange());
        }

        return new WhileLoopStatementNode(keyword, openParen, condition, closeParen, body, TextRange.combine(keyword, body));
    }

    private BreakStatementNode parseBreakStatement() {
        Token keyword = advance(TokenType.BREAK);
        Token semicolon = advance(TokenType.SEMICOLON);
        return new BreakStatementNode(keyword, semicolon, TextRange.combine(keyword, semicolon));
    }

    private ContinueStatementNode parseContinueStatement() {
        Token keyword = advance(TokenType.CONTINUE);
        Token semicolon = advance(TokenType.SEMICOLON);
        return new ContinueStatementNode(keyword, semicolon);
    }

    private EmptyStatementNode parseEmptyStatement() {
        Token semicolon = advance(TokenType.SEMICOLON);
        return new EmptyStatementNode(semicolon);
    }

    private StatementNode parseSimpleStatement() {
        if (isPossibleDeclaration()) {
            return parseVariableDeclaration();
        } else {
            return parseSimpleStatementNotDeclaration(false);
        }
    }

    private boolean isPossibleDeclaration() {
        if (current.is(TokenType.LET)) {
            return true;
        }

        Token next = peek(1);
        // "int." is not declaration
        if (isPredefinedType() && next.isNot(TokenType.DOT)) {
            return true;
        }

        if (current.is(TokenType.IDENTIFIER)) {
            ValueToken identifier = (ValueToken) current;
            if (identifier.value.equals("Java") && next.is(TokenType.LESS)) {
                return true;
            }
            if (identifier.value.equals("fn") && next.is(TokenType.LESS)) {
                return true;
            }
            if (next.is(TokenType.LEFT_SQUARE_BRACKET) && peek(2).is(TokenType.RIGHT_SQUARE_BRACKET)) {
                return true;
            }
            if (next.is(TokenType.IDENTIFIER)) {
                // identifier1 identifier2
                // we have to do more lookahead to correctly handle case like this:
                // a
                // obj.method();
                Token next2 = peek(2);
                return next2.isNot(TokenType.DOT);
            }
        }

        return false;
    }

    private boolean isPossibleFunction() {
        if (current.is(TokenType.ASYNC)) {
            return true;
        }
        if (current.is(TokenType.VOID)) {
            return true;
        }

        LookAhead ahead = new LookAhead();
        try {
            if (!tryAdvanceType()) {
                return false;
            }

            // <type>. - definitely not a function
            if (current.is(TokenType.DOT)) {
                return false;
            }

            // <type> <identifier> "("
            return current.is(TokenType.IDENTIFIER) && peek(1).is(TokenType.LEFT_PARENTHESES);
        } finally {
            ahead.rollback();
        }
    }

    private StaticVariableNode parseStaticVariable() {
        Token keyword = advance(TokenType.STATIC);
        TypeNode type = parseTypeNode();
        if (current.isNot(TokenType.IDENTIFIER)) {
            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRangeAfterLast());
            return new StaticVariableNode(keyword, type, new NameExpressionNode(identifier), null, null, TextRange.combine(keyword, identifier));
        }

        NameExpressionNode name = new NameExpressionNode((ValueToken) advance());
        switch (current.getTokenType()) {
            case SEMICOLON -> {
                Token semicolon = advance();
                return new StaticVariableNode(keyword, type, name, null, null, TextRange.combine(keyword, semicolon));
            }
            case EQUAL -> {
                Token equal = advance();
                if (isPossibleExpression()) {
                    ExpressionNode expression = parseExpression();
                    Token semicolon = advance();
                    return new StaticVariableNode(keyword, type, name, equal, expression, TextRange.combine(keyword, semicolon));
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                    ExpressionNode invalid = new InvalidExpressionNode(createMissingTokenRange());
                    return new StaticVariableNode(keyword, type, name, equal, invalid, TextRange.combine(keyword, invalid));
                }
            }
            default -> {
                addDiagnostic(ParserErrors.SemicolonOrEqualExpected, current, current.getRawValue(code));
                return new StaticVariableNode(keyword, type, name, null, null, TextRange.combine(type, name));
            }
        }
    }

    private ClassNode parseClass() {
        Token keyword = advance(TokenType.CLASS);

        if (current.isNot(TokenType.IDENTIFIER)) {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRange();
            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", range);
            return new ClassNode(
                    keyword,
                    identifier,
                    new Token(TokenType.LEFT_CURLY_BRACKET, range),
                    List.of(),
                    new Token(TokenType.RIGHT_CURLY_BRACKET, range),
                    TextRange.combine(keyword, identifier));
        }

        ValueToken identifier = (ValueToken) advance();
        Token openBrace = advance(TokenType.LEFT_CURLY_BRACKET);
        if (openBrace.getRange().getLength() == 0) {
            return new ClassNode(
                    keyword,
                    identifier,
                    openBrace,
                    List.of(),
                    createMissingTokenAfterLast(TokenType.RIGHT_SQUARE_BRACKET),
                    TextRange.combine(keyword, openBrace));
        }

        List<ClassMemberNode> members = new ArrayList<>();
        while (true) {
            if (current.is(TokenType.END_OF_FILE)) {
                break;
            }
            if (current.is(TokenType.RIGHT_CURLY_BRACKET)) {
                break;
            }
            members.add(parseClassMemberNode());
        }

        Token closeBrace = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new ClassNode(keyword, identifier, openBrace, members, closeBrace, TextRange.combine(keyword, closeBrace));
    }

    private ClassMemberNode parseClassMemberNode() {
        if (current.is(TokenType.CONSTRUCTOR)) {
            return parseClassConstructor();
        }

        if (current.is(TokenType.ASYNC) || current.is(TokenType.VOID)) {
            return parseClassMethod();
        }

        ModifiersNode modifiersNode = parseModifiers();
        if (modifiersNode.getRange().getLength() != 0) {
            throw new InternalException();
        }

        TypeNode typeNode = parseTypeNode();
        if (current.is(TokenType.IDENTIFIER)) {
            ValueToken identifier = (ValueToken) advance();
            if (current.is(TokenType.SEMICOLON)) {
                Token semicolon = advance(TokenType.SEMICOLON);
                return new ClassFieldNode(typeNode, new NameExpressionNode(identifier), semicolon, TextRange.combine(typeNode, semicolon));
            } else if (current.is(TokenType.LEFT_PARENTHESES)) {
                return parseClassMethod(modifiersNode, typeNode, identifier);
            } else {
                if (current.is(TokenType.EQUAL)) {
                    addDiagnostic(ParserErrors.FieldInitializersNotSupported, current);
                } else {
                    addDiagnostic(ParserErrors.SemicolonOrParenthesisExpected, current, current.getRawValue(code));
                }
                Token semicolon = new Token(TokenType.SEMICOLON, createMissingTokenRangeAfterLast());
                return new ClassFieldNode(typeNode, new NameExpressionNode(identifier), semicolon, TextRange.combine(typeNode, semicolon));
            }
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRange());
            Token semicolon = new Token(TokenType.SEMICOLON, createMissingTokenRange());
            return new ClassFieldNode(typeNode, new NameExpressionNode(identifier), semicolon, TextRange.combine(typeNode, semicolon));
        }
    }

    private ClassConstructorNode parseClassConstructor() {
        if (current.isNot(TokenType.CONSTRUCTOR)) {
            throw new InternalException();
        }

        Token keyword = advance();
        ParameterListNode parameters = parseParameterList();

        Token arrow = null;

        StatementNode body;
        if (parameters.hasParentheses()) {
            if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
                body = parseBlockStatement();
            } else if (current.is(TokenType.EQUAL_GREATER)) {
                arrow = advance(TokenType.EQUAL_GREATER);
                body = withSemicolon(parseSimpleStatementNotDeclaration(true));
            } else {
                body = createMissingBlockStatement();
                addDiagnostic(ParserErrors.CurlyBracketOrArrowExpected, current, current.getRawValue(code));
            }
        } else {
            body = createMissingBlockStatement();
        }

        return new ClassConstructorNode(keyword, parameters, arrow, body, TextRange.combine(keyword, body));
    }

    private ClassMethodNode parseClassMethod() {
        ModifiersNode modifiersNode = parseModifiers();
        TypeNode typeNode = parseTypeOrVoidNode();
        ValueToken identifier = (ValueToken) advance();
        return parseClassMethod(modifiersNode, typeNode, identifier);
    }

    private ClassMethodNode parseClassMethod(ModifiersNode modifiersNode, TypeNode typeNode, ValueToken identifier) {
        ParameterListNode parameters = parseParameterList();

        Token arrow = null;

        StatementNode body;
        if (parameters.hasParentheses()) {
            if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
                body = parseBlockStatement();
            } else if (current.is(TokenType.EQUAL_GREATER)) {
                arrow = advance(TokenType.EQUAL_GREATER);
                body = withSemicolon(parseSimpleStatementNotDeclaration(true));
            } else {
                body = createMissingBlockStatement();
                addDiagnostic(ParserErrors.CurlyBracketOrArrowExpected, current, current.getRawValue(code));
            }
        } else {
            body = createMissingBlockStatement();
        }

        return new ClassMethodNode(
                modifiersNode,
                typeNode,
                new NameExpressionNode(identifier),
                parameters,
                arrow,
                body,
                TextRange.combine(modifiersNode, body));
    }

    private FunctionNode parseFunction() {
        ModifiersNode modifiers = parseModifiers();
        TypeNode returnType = parseTypeOrVoidNode();
        if (returnType.isMissing()) {
            return new FunctionNode(
                    modifiers,
                    returnType,
                    createMissingNameExpression(),
                    createMissingParameterList(),
                    null,
                    createMissingInvalidStatement());
        }

        if (current.isNot(TokenType.IDENTIFIER)) {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            return new FunctionNode(
                    modifiers,
                    returnType,
                    createMissingNameExpression(),
                    createMissingParameterList(),
                    null,
                    createMissingInvalidStatement());
        }

        ValueToken identifier = (ValueToken) advance();
        NameExpressionNode name = new NameExpressionNode(identifier);
        ParameterListNode parameters = parseParameterList();

        Token arrow = null;
        StatementNode statement;
        if (parameters.hasParentheses()) {
            if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
                statement = parseBlockStatement();
            } else if (current.is(TokenType.EQUAL_GREATER)) {
                arrow = advance(TokenType.EQUAL_GREATER);
                statement = withSemicolon(parseSimpleStatementNotDeclaration(true));
            } else {
                statement = createMissingBlockStatement();
                addDiagnostic(ParserErrors.CurlyBracketOrArrowExpected, current, current.getRawValue(code));
            }
        } else {
            statement = createMissingBlockStatement();
        }

        return new FunctionNode(modifiers, returnType, name, parameters, arrow, statement);
    }

    private ModifiersNode parseModifiers() {
        if (current.is(TokenType.ASYNC)) {
            Token token = advance();
            return new ModifiersNode(List.of(token), token.getRange());
        } else {
            return new ModifiersNode(List.of(), createMissingTokenRange());
        }
    }

    private ParameterListNode parseParameterList() {
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        if (openParen.getRange().getLength() == 0) {
            Token closeParen = createMissingTokenAfterLast(TokenType.RIGHT_PARENTHESES);
            return new ParameterListNode(
                    openParen,
                    List.of(),
                    closeParen,
                    TextRange.combine(openParen, closeParen));
        }

        Token closeParen;

        List<ParameterNode> parameters = new ArrayList<>();

        if (current.is(TokenType.RIGHT_PARENTHESES)) {
            closeParen = advance();
        } else {
            while (true) {
                TypeNode type = parseRefTypeNode();
                ValueToken identifier;
                if (current.is(TokenType.IDENTIFIER)) {
                    identifier = (ValueToken) advance();
                } else {
                    addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                    identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRange());
                }

                NameExpressionNode name = new NameExpressionNode(identifier);
                parameters.add(new ParameterNode(type, name, TextRange.combine(type, name)));

                if (current.is(TokenType.RIGHT_PARENTHESES)) {
                    closeParen = advance();
                    break;
                } else if (current.is(TokenType.COMMA)) {
                    advance();
                } else {
                    addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                    closeParen = createMissingToken(TokenType.LEFT_PARENTHESES);
                    break;
                }
            }
        }

        return new ParameterListNode(openParen, parameters, closeParen, TextRange.combine(openParen, closeParen));
    }

    private VariableDeclarationNode parseVariableDeclaration() {
        TypeNode type = parseLetTypeNode();
        if (current.is(TokenType.IDENTIFIER)) {
            // we check next token to handle case like this:
            //      int
            //      obj.method();
            // in this case we don't want to assume "obj" is our variable name
            if (peek(1).isNot(TokenType.DOT)) {
                ValueToken identifier = (ValueToken) advance();
                NameExpressionNode name = new NameExpressionNode(identifier);
                switch (current.getTokenType()) {
                    case SEMICOLON -> {
                        if (type.getNodeType() == ParserNodeType.LET_TYPE) {
                            addDiagnostic(ParserErrors.CannotUseLet, type);
                        }
                        return new VariableDeclarationNode(type, name, TextRange.combine(type, name));
                    }
                    case EQUAL -> {
                        advance();
                        if (isPossibleExpression()) {
                            ExpressionNode expression = parseExpression();
                            return new VariableDeclarationNode(type, name, expression, TextRange.combine(type, expression));
                        } else {
                            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                            ExpressionNode invalid = new InvalidExpressionNode(createMissingTokenRange());
                            return new VariableDeclarationNode(type, name, TextRange.combine(type, invalid));
                        }
                    }
                    default -> {
                        addDiagnostic(ParserErrors.SemicolonOrEqualExpected, current, current.getRawValue(code));
                        return new VariableDeclarationNode(type, name, TextRange.combine(type, name));
                    }
                }
            } else {
                addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code) + ".");

                ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRange());
                NameExpressionNode name = new NameExpressionNode(identifier);
                return new VariableDeclarationNode(type, name, TextRange.combine(type, name));
            }
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));

            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRange());
            NameExpressionNode name = new NameExpressionNode(identifier);
            return new VariableDeclarationNode(type, name, TextRange.combine(type, name));
        }
    }

    private StatementNode parseSimpleStatementNotDeclaration(boolean canBeExpression) {
        // TODO: custom types
        ExpressionNode expression1 = parseExpression();
        AssignmentOperator assignment = switch (current.getTokenType()) {
            case EQUAL -> AssignmentOperator.ASSIGNMENT;
            case PLUS_EQUAL -> AssignmentOperator.PLUS_ASSIGNMENT;
            case MINUS_EQUAL -> AssignmentOperator.MINUS_ASSIGNMENT;
            case ASTERISK_EQUAL -> AssignmentOperator.MULTIPLY_ASSIGNMENT;
            case SLASH_EQUAL -> AssignmentOperator.DIVIDE_ASSIGNMENT;
            case PERCENT_EQUAL -> AssignmentOperator.MODULO_ASSIGNMENT;
            case AMPERSAND_EQUAL -> AssignmentOperator.AND_ASSIGNMENT;
            case PIPE_EQUAL -> AssignmentOperator.OR_ASSIGNMENT;
            default -> null;
        };

        if (assignment == null) {
            if (current.is(TokenType.PLUS_PLUS)) {
                Token plusPlus = advance();
                if (!canPostfix(expression1)) {
                    addDiagnostic(ParserErrors.CannotApplyIncDec, expression1);
                }
                return new PostfixStatementNode(ParserNodeType.INCREMENT_STATEMENT, expression1, TextRange.combine(expression1, plusPlus));
            } else if (current.is(TokenType.MINUS_MINUS)) {
                Token minusMinus = advance();
                if (!canPostfix(expression1)) {
                    addDiagnostic(ParserErrors.CannotApplyIncDec, expression1);
                }
                return new PostfixStatementNode(ParserNodeType.DECREMENT_STATEMENT, expression1, TextRange.combine(expression1, minusMinus));
            } else {
                if (!canBeExpression) {
                    ParserNodeType nodeType = expression1.getNodeType();
                    boolean notAStatement =
                            nodeType != ParserNodeType.INVOCATION_EXPRESSION &&
                            nodeType != ParserNodeType.OBJECT_CREATION_EXPRESSION &&
                            nodeType != ParserNodeType.AWAIT_EXPRESSION;
                    if (notAStatement) {
                        addDiagnostic(ParserErrors.NotAStatement, expression1);
                    }
                }
                return new ExpressionStatementNode(expression1, null);
            }
        } else {
            Token assignmentToken = advance();

            ExpressionNode expression2;
            if (isPossibleExpression()) {
                expression2 = parseExpression();
            } else {
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                expression2 = new InvalidExpressionNode(createMissingTokenRange());
            }
            return new AssignmentStatementNode(
                    expression1,
                    new AssignmentOperatorNode(assignmentToken, assignment, assignmentToken.getRange()),
                    expression2,
                    null,
                    TextRange.combine(expression1, expression2));
        }
    }

    private boolean canPostfix(ExpressionNode expression) {
        return expression.is(ParserNodeType.NAME_EXPRESSION) || expression.is(ParserNodeType.INDEX_EXPRESSION) || expression.is(ParserNodeType.MEMBER_ACCESS_EXPRESSION);
    }

    private boolean isPossibleStatement() {
        switch (current.getTokenType()) {
            case LEFT_CURLY_BRACKET:
            case SEMICOLON:
            case IF:
            case RETURN:
            case FOR:
            case FOREACH:
            case WHILE:
            case BREAK:
            case CONTINUE:
            case LET:
                return true;

            default:
                return isPossibleExpression();
        }
    }

    private boolean isPossibleSimpleStatement() {
        switch (current.getTokenType()) {
            case BOOLEAN:
            case INT8:
            case INT16:
            case INT:
            case INT32:
            case INT64:
            case LONG:
            case FLOAT32:
            case FLOAT:
            case FLOAT64:
            case STRING:
            case CHAR:
            case IDENTIFIER:
            case LEFT_PARENTHESES:
            case LET:
                return true;

            default:
                return isPossibleExpression();
        }
    }

    private StatementNode parseStatement() {
        return switch (current.getTokenType()) {
            case LEFT_CURLY_BRACKET -> parseBlockStatement();
            case IF -> parseIfStatement();
            case RETURN -> parseReturnStatement();
            case FOR -> parseForLoopStatement();
            case FOREACH -> parseForEachLoopStatement();
            case WHILE -> parseWhileLoopStatement();
            case BREAK -> parseBreakStatement();
            case CONTINUE -> parseContinueStatement();
            case SEMICOLON -> parseEmptyStatement();
            case BOOLEAN, INT8, INT16, INT, INT32, INT64, LONG, FLOAT32, FLOAT, FLOAT64, STRING, CHAR, IDENTIFIER, LEFT_PARENTHESES -> withSemicolon(parseSimpleStatement());
            case LET -> withSemicolon(parseVariableDeclaration());
            default -> {
                if (isPossibleExpression()) {
                    yield withSemicolon(parseSimpleStatement());
                } else {
                    throw new InternalException("Cannot parse statement.");
                }
            }
        };
    }

    private ExpressionNode parseExpression() {
        return parseExpressionCore(0);
    }

    private ExpressionNode parseExpressionCore(int precedence) {
        ExpressionNode left;

        if (current.is(TokenType.AWAIT)) {
            Token awaitToken = advance();
            ExpressionNode expression = parseExpressionCore(Precedences.getAwait());
            left = new AwaitExpressionNode(awaitToken, expression, TextRange.combine(awaitToken, expression));
        } else {
            UnaryOperator unary = switch (current.getTokenType()) {
                case PLUS -> UnaryOperator.PLUS;
                case MINUS -> UnaryOperator.MINUS;
                case EXCLAMATION -> UnaryOperator.NOT;
                default -> null;
            };

            if (unary != null) {
                Token unaryToken = advance();
                ExpressionNode expression = parseExpressionCore(Precedences.get(unary));
                left = new UnaryExpressionNode(
                        new UnaryOperatorNode(unaryToken, unary),
                        expression,
                        TextRange.combine(unaryToken, expression));
            } else {
                left = parseTerm(precedence);
            }
        }

        return parseExpressionContinued(left, precedence);
    }

    private ExpressionNode parseExpressionContinued(ExpressionNode left, int precedence) {
        while (true) {
            BinaryOperator binary = switch (current.getTokenType()) {
                case PLUS -> BinaryOperator.PLUS;
                case MINUS -> BinaryOperator.MINUS;
                case ASTERISK -> BinaryOperator.MULTIPLY;
                case SLASH -> BinaryOperator.DIVIDE;
                case PERCENT -> BinaryOperator.MODULO;
                case EQUAL_EQUAL -> BinaryOperator.EQUALS;
                case EXCLAMATION_EQUAL -> BinaryOperator.NOT_EQUALS;
                case AMPERSAND -> BinaryOperator.BITWISE_AND;
                case AMPERSAND_AMPERSAND -> BinaryOperator.BOOLEAN_AND;
                case PIPE -> BinaryOperator.BITWISE_OR;
                case PIPE_PIPE -> BinaryOperator.BOOLEAN_OR;
                case LESS -> BinaryOperator.LESS;
                case GREATER -> BinaryOperator.GREATER;
                case LESS_EQUAL -> BinaryOperator.LESS_EQUALS;
                case GREATER_EQUAL -> BinaryOperator.GREATER_EQUALS;
                case IS -> BinaryOperator.IS;
                case AS -> BinaryOperator.AS;
                default -> null;
            };
            if (binary == null) {
                break;
            }

            int newPrecedence = Precedences.get(binary);
            // left/right associativity? equals or not equals?
            if (newPrecedence <= precedence) {
                break;
            }

            Token binaryToken = advance();

            if (binaryToken.is(TokenType.IS)) {
                TypeNode typeNode = parseTypeNode();
                left = new TypeTestExpressionNode(left, typeNode, TextRange.combine(left, typeNode));
            } else if (binaryToken.is(TokenType.AS)) {
                TypeNode typeNode = parseTypeNode();
                left = new TypeCastExpressionNode(left, typeNode, TextRange.combine(left, typeNode));
            } else if (isPossibleExpression()) {
                ExpressionNode right = parseExpressionCore(newPrecedence);
                left = new BinaryExpressionNode(
                        left,
                        new BinaryOperatorNode(binaryToken, binary),
                        right,
                        TextRange.combine(left, right));
            } else {
                left = new BinaryExpressionNode(
                        left,
                        new BinaryOperatorNode(binaryToken, binary),
                        new InvalidExpressionNode(createMissingTokenRange()),
                        TextRange.combine(left, binaryToken));
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                break;
            }
        }

        if (current.is(TokenType.QUESTION) && precedence <= Precedences.getConditionalExpression()) {
            Token question = advance(TokenType.QUESTION);
            ExpressionNode whenTrue = parseExpression();
            Token colon = advance(TokenType.COLON);
            ExpressionNode whenFalse = parseExpression();
            return new ConditionalExpressionNode(left, question, whenTrue, colon, whenFalse, TextRange.combine(left, whenFalse));
        } else {
            return left;
        }
    }

    private ExpressionNode parseTerm(int precedence) {
        return parsePostFixExpression(parseTermWithoutPostfix(precedence));
    }

    private ExpressionNode parsePostFixExpression(ExpressionNode expression) {
        while (true) {
            switch (current.getTokenType()) {
                case LEFT_PARENTHESES -> {
                    ArgumentsListNode arguments = parseArgumentsList();
                    expression = new InvocationExpressionNode(expression, arguments, TextRange.combine(expression, arguments));
                }

                case LEFT_SQUARE_BRACKET -> {
                    Token openBracket = advance(TokenType.LEFT_SQUARE_BRACKET);
                    ExpressionNode index;
                    if (isPossibleExpression()) {
                        index = parseExpression();
                    } else {
                        addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                        index = new InvalidExpressionNode(createMissingTokenRange());
                    }

                    Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);
                    expression = new IndexExpressionNode(expression, openBracket, index, closeBracket);
                }

                case DOT -> {
                    Token dot = advance();
                    if (current.is(TokenType.IDENTIFIER)) {
                        ValueToken identifier = (ValueToken) current;
                        NameExpressionNode name = new NameExpressionNode(identifier.value, identifier.getRange());
                        advance();
                        expression = new MemberAccessExpressionNode(expression, dot, name, TextRange.combine(expression, name));
                    } else {
                        addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                        return new MemberAccessExpressionNode(expression, dot, new NameExpressionNode("", createMissingTokenRangeAfterLast()), TextRange.combine(expression, dot));
                    }
                }

                default -> {
                    return expression;
                }
            }
        }
    }

    private ExpressionNode parseArgumentExpression() {
        if (current.is(TokenType.REF)) {
            Token ref = advance();
            if (current.is(TokenType.IDENTIFIER)) {
                NameExpressionNode name = new NameExpressionNode((ValueToken) advance());
                return new RefArgumentExpressionNode(name, TextRange.combine(ref, name));
            } else {
                addDiagnostic(ParserErrors.InvalidRefExpression, current);
                return new InvalidExpressionNode(ref.getRange());
            }
        } else {
            return parseExpression();
        }
    }

    private ArgumentsListNode parseArgumentsList() {
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        if (openParen.isMissing()) {
            Token closeParen = createMissingTokenAfterLast(TokenType.RIGHT_PARENTHESES);
            return new ArgumentsListNode(
                    openParen,
                    List.of(),
                    closeParen,
                    TextRange.combine(openParen, closeParen));
        }

        List<ExpressionNode> expressions = new ArrayList<>();
        if (current.is(TokenType.RIGHT_PARENTHESES)) {
            Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
            return new ArgumentsListNode(openParen, expressions, closeParen, TextRange.combine(openParen, closeParen));
        }

        if (isPossibleArgumentExpression()) {
            expressions.add(parseArgumentExpression());
        } else {
            addDiagnostic(ParserErrors.ExpressionOrCloseParenthesesExpected, current, current.getRawValue(code));
            Token closeParen = createMissingToken(TokenType.RIGHT_PARENTHESES);
            return new ArgumentsListNode(openParen, expressions, closeParen, TextRange.combine(openParen, closeParen));
        }

        while (true) {
            if (current.is(TokenType.RIGHT_PARENTHESES)) {
                Token closeParen = advance();
                return new ArgumentsListNode(openParen, expressions, closeParen, TextRange.combine(openParen, closeParen));
            }

            if (current.is(TokenType.COMMA)) {
                advance();
                if (isPossibleArgumentExpression()) {
                    expressions.add(parseArgumentExpression());
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                    Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
                    return new ArgumentsListNode(openParen, expressions, closeParen, TextRange.combine(openParen, closeParen));
                }
            } else {
                addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                Token closeParen = createMissingToken(TokenType.RIGHT_PARENTHESES);
                return new ArgumentsListNode(openParen, expressions, closeParen, TextRange.combine(openParen, closeParen));
            }
        }
    }

    private ExpressionNode parseTermWithoutPostfix(int precedence) {
        ExpressionNode expression = switch (current.getTokenType()) {
            case IDENTIFIER -> {
                if (((ValueToken) current).value.equals("Java")) {
                    yield parseStaticReference();
                }
                yield isPossibleLambdaExpression() ? parseLambdaExpression() : new NameExpressionNode((ValueToken) advance());
            }
            case FALSE -> new BooleanLiteralExpressionNode(current, false, advance().getRange());
            case TRUE -> new BooleanLiteralExpressionNode(current, true, advance().getRange());
            case INTEGER_LITERAL -> new IntegerLiteralExpressionNode(null, (ValueToken) advance());
            case INTEGER64_LITERAL -> new Integer64LiteralExpressionNode((ValueToken) advance());
            case FLOAT_LITERAL -> new FloatLiteralExpressionNode((ValueToken) advance());
            case STRING_LITERAL -> new StringLiteralExpressionNode((ValueToken) advance());
            case CHAR_LITERAL -> new CharLiteralExpressionNode((ValueToken) advance());
            case NEW -> parseNewExpression();
            case LEFT_PARENTHESES -> isPossibleLambdaExpression() ? parseLambdaExpression() : parseParenthesizedExpression();
            case LEFT_SQUARE_BRACKET -> parseCollectionExpression();
            case BOOLEAN, INT8, INT16, INT, INT32, INT64, LONG, CHAR, FLOAT32, FLOAT, FLOAT64, STRING -> parseStaticReference();
            case META_UNKNOWN -> new InvalidMetaExpressionNode(advance().getRange());
            case META_TYPE -> parseMetaTypeExpression();
            case META_TYPE_OF -> parseMetaTypeOfExpression();
            case THIS -> new ThisExpressionNode(advance().getRange());
            default -> null;
        };

        if (expression != null) {
            return expression;
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            return new InvalidExpressionNode(createMissingTokenRange());
        }
    }

    private ExpressionNode parseNewExpression() {
        Token newToken = advance(TokenType.NEW);
        TypeNode typeNode = parseTypeNode();

        if (current.is(TokenType.LEFT_SQUARE_BRACKET)) {
            return parseArrayCreationExpression(newToken, typeNode);
        } else if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
            return parseArrayInitializerExpression(newToken, typeNode);
        } else if (current.is(TokenType.LEFT_PARENTHESES)) {
            return parseObjectCreationExpression(newToken, typeNode);
        } else {
            addDiagnostic(ParserErrors.InvalidNewExpression, current);
            return new InvalidExpressionNode(TextRange.combine(newToken, current));
        }
    }

    private ExpressionNode parseArrayCreationExpression(Token newToken, TypeNode typeNode) {
        if (current.isNot(TokenType.LEFT_SQUARE_BRACKET)) {
            throw new InternalException();
        }

        Token openBracket = advance(TokenType.LEFT_SQUARE_BRACKET);

        ExpressionNode lengthExpression;
        if (isPossibleExpression()) {
            lengthExpression = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            lengthExpression = new InvalidExpressionNode(createMissingTokenRange());
        }

        Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);

        return new ArrayCreationExpressionNode(
                newToken,
                typeNode,
                openBracket,
                lengthExpression,
                closeBracket,
                TextRange.combine(newToken, last));
    }

    private ExpressionNode parseArrayInitializerExpression(Token newToken, TypeNode typeNode) {
        if (current.isNot(TokenType.LEFT_CURLY_BRACKET)) {
            throw new InternalException();
        }

        Token openBrace = advance(TokenType.LEFT_CURLY_BRACKET);
        Token closeBrace;

        List<ExpressionNode> items = new ArrayList<>();
        boolean expectExpression = true;
        while (true) {
            if (current.is(TokenType.END_OF_FILE)) {
                closeBrace = createMissingTokenAfterLast(TokenType.RIGHT_CURLY_BRACKET);
                break;
            }

            if (current.is(TokenType.RIGHT_CURLY_BRACKET)) {
                closeBrace = advance(TokenType.RIGHT_CURLY_BRACKET);
                break;
            }

            if (expectExpression) {
                if (isPossibleExpression()) {
                    items.add(parseExpression());
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                }
                expectExpression = false;
            } else {
                if (current.is(TokenType.COMMA)) {
                    advance(TokenType.COMMA);
                    expectExpression = true;
                } else {
                    closeBrace = createMissingTokenAfterLast(TokenType.RIGHT_CURLY_BRACKET);
                    addDiagnostic(ParserErrors.CommaOrCloseCurlyBracketExpected, current);
                    break;
                }
            }
        }

        return new ArrayInitializerExpressionNode(newToken, typeNode, openBrace, items, closeBrace, TextRange.combine(newToken, last));
    }

    private ExpressionNode parseObjectCreationExpression(Token newToken, TypeNode typeNode) {
        if (current.isNot(TokenType.LEFT_PARENTHESES)) {
            throw new InternalException();
        }

        ArgumentsListNode arguments = parseArgumentsList();
        return new ObjectCreationExpressionNode(typeNode, arguments, TextRange.combine(newToken, arguments));
    }

    private ExpressionNode parseParenthesizedExpression() {
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        ExpressionNode expression = parseExpression();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new ParenthesizedExpressionNode(expression, TextRange.combine(openParen, closeParen));
    }

    private ExpressionNode parseCollectionExpression() {
        Token openBracket = advance(TokenType.LEFT_SQUARE_BRACKET);

        List<ExpressionNode> items = new ArrayList<>();
        boolean expectExpression = true;
        while (current.isNot(TokenType.END_OF_FILE)) {
            if (expectExpression) {
                if (current.is(TokenType.RIGHT_SQUARE_BRACKET)) {
                    break;
                }
                if (isPossibleExpression()) {
                    items.add(parseExpression());
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                }
                expectExpression = false;
            } else {
                if (current.is(TokenType.RIGHT_SQUARE_BRACKET)) {
                    break;
                }
                if (current.is(TokenType.COMMA)) {
                    advance(TokenType.COMMA);
                    expectExpression = true;
                } else {
                    addDiagnostic(ParserErrors.CommaOrCloseSquareBracketExpected, current);
                    break;
                }
            }
        }

        Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);

        return new CollectionExpressionNode(openBracket, items, closeBracket, TextRange.combine(openBracket, closeBracket));
    };

    private LambdaExpressionNode parseLambdaExpression() {
        Token first = current;
        List<NameExpressionNode> parameters = new ArrayList<>();
        Token arrow;
        if (current.is(TokenType.IDENTIFIER)) {
            parameters.add(new NameExpressionNode((ValueToken) advance()));
            arrow = advance(TokenType.EQUAL_GREATER);
        } else if (current.is(TokenType.LEFT_PARENTHESES)) {
            advance();
            if (current.is(TokenType.RIGHT_PARENTHESES)) {
                advance();
            } else {
                while (true) {
                    if (current.is(TokenType.IDENTIFIER)) {
                        parameters.add(new NameExpressionNode((ValueToken) advance()));
                        if (current.is(TokenType.RIGHT_PARENTHESES)) {
                            advance();
                            break;
                        } else if (current.is(TokenType.COMMA)) {
                            advance();
                        } else {
                            addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                            break;
                        }
                    }
                }
            }
            arrow = advance(TokenType.EQUAL_GREATER);
        } else {
            throw new InternalException("Check isPossibleLambdaExpression() method.");
        }

        StatementNode statement;
        if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
            statement = parseBlockStatement();
        } else {
            if (isPossibleSimpleStatement() && !isPossibleDeclaration()) {
                statement = parseSimpleStatementNotDeclaration(true);
            } else {
                statement = new InvalidStatementNode(createMissingTokenRange());
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
            }
        }

        return new LambdaExpressionNode(parameters, arrow, statement, TextRange.combine(first, statement));
    }

    private StaticReferenceNode parseStaticReference() {
        TypeNode typeNode = parseTypeNode();
        return new StaticReferenceNode(typeNode, typeNode.getRange());
    }

    private MetaTypeExpressionNode parseMetaTypeExpression() {
        Token keyword = advance(TokenType.META_TYPE);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        TypeNode type = parseTypeNode();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new MetaTypeExpressionNode(keyword, openParen, type, closeParen, TextRange.combine(keyword, closeParen));
    }

    private MetaTypeOfExpressionNode parseMetaTypeOfExpression() {
        Token keyword = advance(TokenType.META_TYPE_OF);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        ExpressionNode expression = parseExpression();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new MetaTypeOfExpressionNode(keyword, openParen, expression, closeParen, TextRange.combine(keyword, closeParen));
    }

    private boolean isPossibleExpression() {
        switch (current.getTokenType()) {
            case FALSE:
            case TRUE:
            case LEFT_PARENTHESES:
            case LEFT_SQUARE_BRACKET:
            case INTEGER_LITERAL:
            case INTEGER64_LITERAL:
            case FLOAT_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
            case NEW:
            case IDENTIFIER:
            case BOOLEAN:
            case INT8:
            case INT16:
            case INT:
            case INT32:
            case INT64:
            case LONG:
            case FLOAT32:
            case FLOAT:
            case FLOAT64:
            case STRING:
            case CHAR:
            case PLUS:
            case MINUS:
            case EXCLAMATION:
            case AWAIT:
            case META_UNKNOWN:
            case META_TYPE:
            case META_TYPE_OF:
            case THIS:
                return true;

            default:
                return false;
        }
    }

    private boolean isPossibleArgumentExpression() {
        if (current.is(TokenType.REF)) {
            return true;
        } else {
            return isPossibleExpression();
        }
    }

    private boolean isPossibleLambdaExpression() {
        // x => ...
        if (current.is(TokenType.IDENTIFIER)) {
            return peek(1).is(TokenType.EQUAL_GREATER);
        }

        if (current.is(TokenType.LEFT_PARENTHESES)) {
            // () => ...
            if (peek(1).is(TokenType.RIGHT_PARENTHESES) && peek(2).is(TokenType.EQUAL_GREATER)) {
                return true;
            }

            // (x) => ...
            // (x, y, z) => ...
            if (peek(1).is(TokenType.IDENTIFIER)) {
                boolean commaExpected = true;
                int index = 2;
                while (true) {
                    Token next = peek(index++);
                    if (commaExpected) {
                        if (next.is(TokenType.COMMA)) {
                            commaExpected = false;
                        } else if (next.is(TokenType.RIGHT_PARENTHESES)) {
                            return peek(index).is(TokenType.EQUAL_GREATER);
                        } else {
                            return false;
                        }
                    } else {
                        if (next.is(TokenType.IDENTIFIER)) {
                            commaExpected = true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isPredefinedType() {
        switch (current.getTokenType()) {
            case BOOLEAN:
            case INT8:
            case INT16:
            case INT:
            case INT32:
            case INT64:
            case LONG:
            case FLOAT32:
            case FLOAT:
            case FLOAT64:
            case STRING:
            case CHAR:
                return true;

            default:
                return false;
        }
    }

    private TypeNode parseRefTypeNode() {
        if (current.is(TokenType.REF)) {
            Token ref = advance();
            TypeNode underlying = parseTypeNode();
            return new RefTypeNode(underlying, TextRange.combine(ref, underlying));
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseLetTypeNode() {
        if (current.is(TokenType.LET)) {
            Token token = advance();
            return new LetTypeNode(token.getRange());
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseTypeOrVoidNode() {
        if (current.is(TokenType.VOID)) {
            return new VoidTypeNode(advance().getRange());
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseTypeNode() {
        TypeNode type = switch (current.getTokenType()) {
            case BOOLEAN -> new PredefinedTypeNode(PredefinedType.BOOLEAN, advance().getRange());
            case INT8 -> new PredefinedTypeNode(PredefinedType.INT8, advance().getRange());
            case INT16 -> new PredefinedTypeNode(PredefinedType.INT16, advance().getRange());
            case INT, INT32 -> new PredefinedTypeNode(PredefinedType.INT, advance().getRange());
            case INT64, LONG -> new PredefinedTypeNode(PredefinedType.INT64, advance().getRange());
            case FLOAT32 -> new PredefinedTypeNode(PredefinedType.FLOAT32, advance().getRange());
            case FLOAT, FLOAT64 -> new PredefinedTypeNode(PredefinedType.FLOAT, advance().getRange());
            case STRING -> new PredefinedTypeNode(PredefinedType.STRING, advance().getRange());
            case CHAR -> new PredefinedTypeNode(PredefinedType.CHAR, advance().getRange());
            case IDENTIFIER -> {
                ValueToken identifier = (ValueToken) current;
                if (identifier.value.equals("Java")) {
                    yield parseJavaType();
                } else if (identifier.value.equals("fn")) {
                    yield parseFunctionType();
                } else {
                    advance();
                    yield new CustomTypeNode(identifier);
                }
            }
            default -> {
                addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code));
                if (current.is(TokenType.RIGHT_PARENTHESES) || current.is(TokenType.END_OF_FILE)) {
                    // TODO: add other token types
                    yield new InvalidTypeNode(createMissingTokenRangeAfterLast());
                } else {
                    yield new InvalidTypeNode(advance().getRange());
                }
            }
        };

        while (true) {
            if (current.is(TokenType.LEFT_SQUARE_BRACKET) && peek(1).is(TokenType.RIGHT_SQUARE_BRACKET)) {
                Token openBracket = advance(TokenType.LEFT_SQUARE_BRACKET);
                Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);
                type = new ArrayTypeNode(type, openBracket, closeBracket, TextRange.combine(type, closeBracket));
            } else {
                break;
            }
        }

        return type;
    }

    private JavaTypeNode parseJavaType() {
        Token begin = advance(TokenType.IDENTIFIER);
        Token open = advance(TokenType.LESS);
        Token close = null;

        JavaQualifiedTypeNameNode qualifiedTypeName;
        if (!open.getRange().isEmpty()) {
            qualifiedTypeName = parseQualifiedTypeName();
            close = advance(TokenType.GREATER);
        } else {
            qualifiedTypeName = new JavaQualifiedTypeNameNode("", createMissingTokenRange());
        }

        if (close == null) {
            close = createMissingToken(TokenType.GREATER);
        }

        return new JavaTypeNode(open, qualifiedTypeName, close, TextRange.combine(begin, last));
    }

    private JavaQualifiedTypeNameNode parseQualifiedTypeName() {
        final int STATE_START = 1;
        final int STATE_IDENTIFIER_READ = 2;
        final int STATE_SEPARATOR_READ = 3;
        final int STATE_END = 10;

        StringBuilder sb = new StringBuilder();
        TextRange nameRange = null;

        int state = STATE_START;
        while (state != STATE_END) {
            switch (state) {
                case STATE_START, STATE_SEPARATOR_READ -> {
                    if (current.is(TokenType.IDENTIFIER)) {
                        if (state == STATE_START) {
                            nameRange = current.getRange();
                        } else {
                            nameRange = TextRange.combine(nameRange, current.getRange());
                        }

                        sb.append(((ValueToken) advance()).value);
                        state = STATE_IDENTIFIER_READ;
                    } else {
                        addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                        state = STATE_END;
                    }
                }
                case STATE_IDENTIFIER_READ -> {
                    if (current.is(TokenType.GREATER)) {
                        state = STATE_END;
                    } else if (current.is(TokenType.DOT)) {
                        nameRange = TextRange.combine(nameRange, current.getRange());
                        advance();
                        sb.append('.');
                        state = STATE_SEPARATOR_READ;
                    } else if (current.is(TokenType.DOLLAR)) {
                        nameRange = TextRange.combine(nameRange, current.getRange());
                        advance();
                        sb.append('$');
                        state = STATE_SEPARATOR_READ;
                    } else {
                        // error will be added later
                        state = STATE_END;
                    }
                }
            }
        }

        if (nameRange != null) {
            return new JavaQualifiedTypeNameNode(sb.toString(), nameRange);
        } else {
            return new JavaQualifiedTypeNameNode("", createMissingTokenRangeAfterLast());
        }
    }

    private FunctionTypeNode parseFunctionType() {
        if (current.isNot(TokenType.IDENTIFIER)) {
            throw new InternalException();
        }

        ValueToken fn = (ValueToken) advance(TokenType.IDENTIFIER);
        if (!fn.value.equals("fn")) {
            throw new InternalException();
        }

        Token openBracket = advance(TokenType.LESS);
        if (openBracket.getRange().isEmpty()) {
            TextRange missing = createMissingTokenRangeAfterLast();
            return new FunctionTypeNode(
                    fn,
                    openBracket,
                    null,
                    SeparatedList.of(),
                    null,
                    new Token(TokenType.EQUAL_GREATER, missing),
                    new InvalidTypeNode(missing),
                    new Token(TokenType.GREATER, missing));
        }

        Token openParen = null;
        SeparatedList<TypeNode> parameters;
        Token closeParen = null;

        if (current.is(TokenType.LEFT_PARENTHESES)) {
            openParen = advance(TokenType.LEFT_PARENTHESES);
            parameters = parseSeparatedList(
                    this::parseTypeNode,
                    this::tryAdvanceType,
                    () -> addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code)),
                    () -> addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code)),
                    TokenType.RIGHT_PARENTHESES);
            closeParen = advance(TokenType.RIGHT_PARENTHESES);
        } else {
            parameters = new SeparatedList<>();
            parameters.add(parseTypeNode());
        }

        Token arrow = advance(TokenType.EQUAL_GREATER);

        if (arrow.getRange().isEmpty()) {
            TextRange missing = createMissingTokenRangeAfterLast();
            return new FunctionTypeNode(
                    fn,
                    openBracket,
                    openParen,
                    parameters,
                    closeParen,
                    arrow,
                    new InvalidTypeNode(missing),
                    new Token(TokenType.GREATER, missing));
        }

        TypeNode returnTypeNode = parseTypeOrVoidNode();
        if (returnTypeNode.getRange().isEmpty()) {
            return new FunctionTypeNode(
                    fn,
                    openBracket,
                    openParen,
                    parameters,
                    closeParen,
                    arrow,
                    returnTypeNode,
                    createMissingTokenAfterLast(TokenType.GREATER));
        }

        Token closeBracket = advance(TokenType.GREATER);
        return new FunctionTypeNode(
                fn,
                openBracket,
                openParen,
                parameters,
                closeParen,
                arrow,
                returnTypeNode,
                closeBracket);
    }

    private boolean tryAdvanceType() {
        if (isPredefinedType()) {
            advance();
            advanceArrayMarkers();
            return true;
        } else if (current.is(TokenType.IDENTIFIER)) {
            ValueToken identifier = (ValueToken) advance();
            if (identifier.value.equals("Java")) {
                if (current.is(TokenType.LESS)) {
                    advance();
                    advanceQualifiedTypeName();
                    if (current.is(TokenType.GREATER)) {
                        advance();
                    }
                }
            }
            if (identifier.value.equals("fn")) {
                if (current.is(TokenType.LESS)) {
                    advance();
                    advanceFnType();
                }
            }
            advanceArrayMarkers();
            return true;
        } else {
            return false;
        }
    }

    private void advanceQualifiedTypeName() {
        final int STATE_START = 1;
        final int STATE_IDENTIFIER_READ = 2;
        final int STATE_SEPARATOR_READ = 3;
        final int STATE_END = 10;

        int state = STATE_START;
        while (state != STATE_END) {
            switch (state) {
                case STATE_START, STATE_SEPARATOR_READ -> {
                    if (current.is(TokenType.IDENTIFIER)) {
                        advance();
                        state = STATE_IDENTIFIER_READ;
                    } else {
                        state = STATE_END;
                    }
                }
                case STATE_IDENTIFIER_READ -> {
                    if (current.is(TokenType.GREATER)) {
                        state = STATE_END;
                    } else if (current.is(TokenType.DOT)) {
                        advance();
                        state = STATE_SEPARATOR_READ;
                    } else if (current.is(TokenType.DOLLAR)) {
                        advance();
                        state = STATE_SEPARATOR_READ;
                    } else {
                        state = STATE_END;
                    }
                }
            }
        }
    }

    private void advanceFnType() {
        if (current.is(TokenType.LEFT_PARENTHESES)) {
            // () => type
            // (type1, type2) => type3
            advance();

            final int STATE_BEGIN = 1;
            final int STATE_TYPE_READ = 2;
            final int STATE_COMMA_READ = 3;
            final int STATE_END = 4;

            int state = STATE_BEGIN;
            while (state != STATE_END) {
                switch (state) {
                    case STATE_BEGIN -> {
                        if (current.is(TokenType.RIGHT_PARENTHESES)) {
                            advance();
                            state = STATE_END;
                            break;
                        }
                        if (tryAdvanceType()) {
                            state = STATE_TYPE_READ;
                            break;
                        }
                        return;
                    }
                    case STATE_TYPE_READ -> {
                        if (current.is(TokenType.RIGHT_PARENTHESES)) {
                            advance();
                            state = STATE_END;
                            break;
                        }
                        if (current.is(TokenType.COMMA)) {
                            advance();
                            state = STATE_COMMA_READ;
                            break;
                        }
                        return;
                    }
                    case STATE_COMMA_READ -> {
                        if (tryAdvanceType()) {
                            state = STATE_TYPE_READ;
                            break;
                        }
                        return;
                    }
                }
            }
        } else {
            // type1 => type2
            if (!tryAdvanceType()) {
                return;
            }
        }

        if (current.isNot(TokenType.EQUAL_GREATER)) {
            return;
        }
        advance();

        if (!tryAdvanceType()) {
            return;
        }

        if (current.isNot(TokenType.GREATER)) {
            return;
        }
        advance();
    }

    private void advanceArrayMarkers() {
        while (true) {
            if (current.is(TokenType.LEFT_SQUARE_BRACKET) && peek(1).is(TokenType.RIGHT_SQUARE_BRACKET)) {
                advance();
                advance();
            } else {
                break;
            }
        }
    }

    private <T extends ParserNode> SeparatedList<T> parseSeparatedList(
            Supplier<T> parseNode,
            BooleanSupplier tryAdvanceNode,
            Runnable onCannotAdvanceNode,
            Runnable onCannotAdvanceComma,
            TokenType endTokenType
    ) {
        SeparatedList<T> list = new SeparatedList<>();

        final int STATE_BEGIN = 1;
        final int STATE_READ_ITEM = 2;
        final int STATE_READ_COMMA = 3;
        final int STATE_END = 4;

        int state = STATE_BEGIN;
        while (state != STATE_END) {
            switch (state) {
                case STATE_BEGIN -> {
                    if (current.is(endTokenType)) {
                        state = STATE_END;
                        break;
                    }

                    LookAhead ahead = new LookAhead();
                    try {
                        if (!tryAdvanceNode.getAsBoolean()) {
                            state = STATE_END;
                        }
                    } finally {
                        ahead.rollback();
                    }

                    if (state == STATE_END) {
                        onCannotAdvanceNode.run();
                    } else {
                        list.add(parseNode.get());
                        state = STATE_READ_ITEM;
                    }
                }
                case STATE_READ_ITEM -> {
                    if (current.is(TokenType.COMMA)) {
                        list.add(advance(TokenType.COMMA));
                        state = STATE_READ_COMMA;
                        break;
                    }
                    if (current.is(endTokenType)) {
                        state = STATE_END;
                        break;
                    }
                    onCannotAdvanceComma.run();
                    state = STATE_END;
                }
                case STATE_READ_COMMA -> {
                    LookAhead ahead = new LookAhead();
                    try {
                        if (!tryAdvanceNode.getAsBoolean()) {
                            state = STATE_END;
                        }
                    } finally {
                        ahead.rollback();
                    }

                    if (state == STATE_END) {
                        onCannotAdvanceNode.run();
                    } else {
                        list.add(parseNode.get());
                        state = STATE_READ_ITEM;
                    }
                }
            }
        }

        return list;
    }

    private Token advance() {
        last = current;

        Token match = current;
        current = tokens.next();
        return match;
    }

    private Token advance(TokenType type) {
        if (current.is(type)) {
            last = current;

            Token match = current;
            current = tokens.next();
            return match;
        } else {
            switch (type) {
                case LEFT_PARENTHESES -> addDiagnostic(ParserErrors.LeftParenthesisExpected, current, current.getRawValue(code));
                case RIGHT_PARENTHESES -> addDiagnostic(ParserErrors.RightParenthesisExpected, current, current.getRawValue(code));
                case LEFT_CURLY_BRACKET -> addDiagnostic(ParserErrors.OpenCurlyBracketExpected, current, current.getRawValue(code));
                case RIGHT_CURLY_BRACKET -> addDiagnostic(ParserErrors.CloseCurlyBracketExpected, current, current.getRawValue(code));
                case RIGHT_SQUARE_BRACKET -> addDiagnostic(ParserErrors.CloseSquareBracketExpected, current, current.getRawValue(code));
                case LESS -> addDiagnostic(ParserErrors.OpenTriangleBracketExpected, current, current.getRawValue(code));
                case GREATER -> addDiagnostic(ParserErrors.CloseTriangleBracketExpected, current, current.getRawValue(code));
                case SEMICOLON -> addDiagnostic(ParserErrors.SemicolonExpected, last);
                case COLON -> addDiagnostic(ParserErrors.ColonExpected, last);
                case IN -> addDiagnostic(ParserErrors.InExpected, current);
                case EQUAL_GREATER -> addDiagnostic(ParserErrors.ArrowExpected, current);
                default -> throw new RuntimeException("Not implemented");
            }

            return switch (type) {
                case LEFT_PARENTHESES, SEMICOLON, GREATER, EQUAL_GREATER -> createMissingTokenAfterLast(type);
                default -> createMissingToken(type);
            };
        }
    }

    private Token peek(int n) {
        return tokens.peek(n);
    }

    private StatementNode withSemicolon(StatementNode statement) {
        if (statement.isOpen()) {
            return statement;
        } else {
            return statement.updateWithSemicolon(advance(TokenType.SEMICOLON));
        }
    }

    private void addDiagnostic(ErrorCode code, Locatable locatable, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, locatable, parameters));
    }

    private NameExpressionNode createMissingNameExpression() {
        return new NameExpressionNode("", createMissingTokenRangeAfterLast());
    }

    private ParameterListNode createMissingParameterList() {
        return new ParameterListNode(
                createMissingTokenAfterLast(TokenType.LEFT_PARENTHESES),
                List.of(),
                createMissingTokenAfterLast(TokenType.RIGHT_PARENTHESES),
                createMissingTokenRangeAfterLast());
    }

    private InvalidStatementNode createMissingInvalidStatement() {
        return new InvalidStatementNode(createMissingTokenRangeAfterLast());
    }

    private Token createMissingToken(TokenType type) {
        return new Token(type, createMissingTokenRange());
    }

    private Token createMissingTokenAfterLast(TokenType type) {
        return new Token(type, createMissingTokenRangeAfterLast());
    }

    private TextRange createMissingTokenRangeAfterLast() {
        return new SingleLineTextRange(
                last.getRange().getLine2(),
                last.getRange().getColumn2(),
                last.getRange().getPosition() + last.getRange().getLength(),
                0);
    }

    private TextRange createMissingTokenRange() {
        return new SingleLineTextRange(
                current.getRange().getLine1(),
                current.getRange().getColumn1(),
                current.getRange().getPosition(),
                0);
    }

    private class LookAhead {

        private final Token current;
        private final Token last;
        private final int position;

        public LookAhead() {
            current = Parser.this.current;
            last = Parser.this.last;
            position = Parser.this.tokens.position();
        }

        public void rollback() {
            Parser.this.current = current;
            Parser.this.last = last;
            Parser.this.tokens.rollback(position);
        }
    }
}