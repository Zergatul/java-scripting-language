package com.zergatul.scripting.parser;

import com.zergatul.scripting.*;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.parser.nodes.PatternNode;

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
            } else if (current.is(TokenType.EXTENSION)) {
                members.add(parseExtension());
            } else if (current.is(TokenType.CLASS)) {
                members.add(parseClass());
            } else if (current.is(TokenType.TYPEALIAS)) {
                members.add(parseTypeAlias());
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
        return new BlockStatementNode(openBrace, statements, closeBrace);
    }

    private BlockStatementNode createMissingBlockStatement() {
        return createMissingBlockStatement(createMissingTokenRangeAfterLast());
    }

    private BlockStatementNode createMissingBlockStatement(TextRange range) {
        return new BlockStatementNode(
                new Token(TokenType.LEFT_CURLY_BRACKET, range),
                List.of(),
                new Token(TokenType.RIGHT_CURLY_BRACKET, range));
    }

    private IfStatementNode parseIfStatement() {
        Token ifToken = advance(TokenType.IF);

        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        if (openParen.getRange().isEmpty()) {
            // assume entire statement is missing
            return new IfStatementNode(
                    ifToken,
                    openParen,
                    new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent()),
                    createMissingTokenBeforeCurrent(TokenType.RIGHT_PARENTHESES),
                    new InvalidStatementNode(createMissingTokenRangeBeforeCurrent()),
                    null,
                    null,
                    ifToken.getRange());
        }

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
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
                elseStatement = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
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

        Token semicolon;
        ExpressionNode expression = null;
        if (isPossibleExpression()) {
            expression = parseExpression();
            if (expression.isOpen()) {
                if (current.is(TokenType.SEMICOLON)) {
                    semicolon = advance();
                } else {
                    semicolon = new Token(TokenType.SEMICOLON, expression.getRange().getEnd());
                }
            } else {
                semicolon = advance(TokenType.SEMICOLON);
            }
        } else {
            semicolon = advance(TokenType.SEMICOLON);
        }

        return new ReturnStatementNode(keyword, expression, semicolon);
    }

    private ForLoopStatementNode parseForLoopStatement() {
        Token keyword = advance(TokenType.FOR);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);

        if (openParen.getRange().isEmpty()) {
            // assume entire statement is missing
            TextRange range = createMissingTokenRangeAfterLast();
            return new ForLoopStatementNode(
                    keyword,
                    openParen,
                    new InvalidStatementNode(range),
                    new Token(TokenType.SEMICOLON, range),
                    new InvalidExpressionNode(range),
                    new Token(TokenType.SEMICOLON, range),
                    new InvalidStatementNode(range),
                    new Token(TokenType.RIGHT_PARENTHESES, range),
                    new InvalidStatementNode(range));
        }

        StatementNode init = null;
        if (current.isNot(TokenType.SEMICOLON)) {
            if (isPossibleSimpleStatementOrDeclaration()) {
                init = parseSimpleStatementOrDeclaration();
            } else {
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
                init = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
            }
        }

        Token semicolon1 = advance(TokenType.SEMICOLON);

        ExpressionNode condition = null;
        if (current.isNot(TokenType.SEMICOLON)) {
            if (isPossibleExpression()) {
                condition = parseExpression();
            } else {
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            }
        }

        Token semicolon2 = advance(TokenType.SEMICOLON);

        StatementNode update;
        if (current.is(TokenType.RIGHT_PARENTHESES)) {
            update = null;
        } else {
            if (isPossibleSimpleStatementNotDeclaration()) {
                update = parseSimpleStatementNotDeclaration(false);
            } else {
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
                update = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
            }
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
        }

        return new ForLoopStatementNode(keyword, openParen, init, semicolon1, condition, semicolon2, update, closeParen, body);
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
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    new NameExpressionNode(createMissingIdentifier(range)),
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
            typeNode = new InvalidTypeNode(createMissingIdentifierAfterLast());
        }

        ValueToken identifier;
        if (current.is(TokenType.IDENTIFIER)) {
            identifier = (ValueToken) advance();
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRangeBeforeCurrent());
        }

        Token in = advance(TokenType.IN);

        ExpressionNode iterable;
        if (isPossibleExpression()) {
            iterable = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            iterable = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
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
                    new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent()),
                    new Token(TokenType.RIGHT_PARENTHESES, createMissingTokenRangeBeforeCurrent()),
                    new InvalidStatementNode(createMissingTokenRangeBeforeCurrent()));
        }

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
        }

        return new WhileLoopStatementNode(keyword, openParen, condition, closeParen, body);
    }

    private TryStatementNode parseTryStatement() {
        Token keyword = advance(TokenType.TRY);
        if (current.isNot(TokenType.LEFT_CURLY_BRACKET)) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new TryStatementNode(
                    keyword,
                    createMissingBlockStatement(range),
                    null,
                    new FinallyClauseNode(
                            new Token(TokenType.FINALLY, range),
                            createMissingBlockStatement(range)));
        }

        BlockStatementNode block = parseBlockStatement();
        if (block.isOpen()) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new TryStatementNode(
                    keyword,
                    block,
                    null,
                    new FinallyClauseNode(
                            new Token(TokenType.FINALLY, range),
                            createMissingBlockStatement(range)));
        }

        if (current.isNot(TokenType.CATCH) && current.isNot(TokenType.FINALLY)) {
            addDiagnostic(ParserErrors.CatchOrFinallyExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new TryStatementNode(
                    keyword,
                    block,
                    null,
                    new FinallyClauseNode(
                            new Token(TokenType.FINALLY, range),
                            createMissingBlockStatement(range)));
        }

        CatchClauseNode catchClause = null;
        if (current.is(TokenType.CATCH)) {
            catchClause = parseCatchClause();
            if (catchClause.isOpen()) {
                return new TryStatementNode(
                        keyword,
                        block,
                        catchClause,
                        null);
            }
        }

        FinallyClauseNode finallyClause = null;
        if (current.is(TokenType.FINALLY)) {
            finallyClause = parseFinallyClause();
        }

        return new TryStatementNode(keyword, block, catchClause, finallyClause);
    }

    private CatchClauseNode parseCatchClause() {
        Token keyword = advance(TokenType.CATCH);
        if (current.isNot(TokenType.LEFT_PARENTHESES) && current.isNot(TokenType.LEFT_CURLY_BRACKET)) {
            addDiagnostic(ParserErrors.OpenParenOrBraceExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new CatchClauseNode(
                    keyword,
                    null,
                    createMissingBlockStatement(range));
        }

        CatchDeclarationNode declaration = null;
        if (current.is(TokenType.LEFT_PARENTHESES)) {
            declaration = parseCatchDeclaration();
            if (declaration.isOpen()) {
                TextRange range = createMissingTokenRangeBeforeCurrent();
                return new CatchClauseNode(
                        keyword,
                        declaration,
                        createMissingBlockStatement(range));
            }
        }

        assert current.is(TokenType.LEFT_CURLY_BRACKET);
        BlockStatementNode block = parseBlockStatement();

        return new CatchClauseNode(keyword, declaration, block);
    }

    private CatchDeclarationNode parseCatchDeclaration() {
        Token openParen = advance(TokenType.LEFT_PARENTHESES);

        if (current.isNot(TokenType.IDENTIFIER)) {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            Token closeParen = current.is(TokenType.RIGHT_PARENTHESES) ? advance() : new Token(TokenType.RIGHT_PARENTHESES, range);
            return new CatchDeclarationNode(
                    openParen,
                    createMissingIdentifier(range),
                    closeParen);
        }

        ValueToken identifier = (ValueToken) advance();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        return new CatchDeclarationNode(openParen, identifier, closeParen);
    }

    private FinallyClauseNode parseFinallyClause() {
        Token keyword = advance(TokenType.FINALLY);
        if (current.isNot(TokenType.LEFT_CURLY_BRACKET)) {
            addDiagnostic(ParserErrors.OpenCurlyBracketExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new FinallyClauseNode(
                    keyword,
                    createMissingBlockStatement(range));
        }

        return new FinallyClauseNode(keyword, parseBlockStatement());
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

    private StatementNode parseSimpleStatementOrDeclaration() {
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

        LookAhead ahead = new LookAhead();
        try {
            if (!tryAdvanceType()) {
                return false;
            }

            if (current.is(TokenType.DOT)) {
                // <type> .
                return false;
            }

            if (!current.is(TokenType.IDENTIFIER)) {
                return false;
            }

            advance();

            // <type>
            // <identifier>.method();
            return !current.is(TokenType.DOT) && !current.is(TokenType.DOT_HASH);
        } finally {
            ahead.rollback();
        }
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
            TextRange missing = createMissingTokenRangeAfterLast();
            return new StaticVariableNode(
                    keyword,
                    type,
                    createMissingNameExpression(missing),
                    null,
                    null,
                    new Token(TokenType.SEMICOLON, missing));
        }

        NameExpressionNode name = new NameExpressionNode((ValueToken) advance());
        switch (current.getTokenType()) {
            case SEMICOLON -> {
                Token semicolon = advance(TokenType.SEMICOLON);
                return new StaticVariableNode(keyword, type, name, null, null, semicolon);
            }
            case EQUAL -> {
                Token equal = advance(TokenType.EQUAL);
                if (isPossibleExpression()) {
                    ExpressionNode expression = parseExpression();
                    Token semicolon = advance(TokenType.SEMICOLON);
                    return new StaticVariableNode(keyword, type, name, equal, expression, semicolon);
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                    ExpressionNode invalid = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
                    Token semicolon = createMissingTokenAfterLast(TokenType.SEMICOLON);
                    return new StaticVariableNode(keyword, type, name, equal, invalid, semicolon);
                }
            }
            default -> {
                addDiagnostic(ParserErrors.SemicolonOrEqualExpected, current, current.getRawValue(code));
                Token semicolon = createMissingTokenAfterLast(TokenType.SEMICOLON);
                return new StaticVariableNode(keyword, type, name, null, null, semicolon);
            }
        }
    }

    private ExtensionNode parseExtension() {
        Token keyword = advance(TokenType.EXTENSION);

        if (current.isNot(TokenType.LEFT_PARENTHESES)) {
            addDiagnostic(ParserErrors.LeftParenthesisExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ExtensionNode(
                    keyword,
                    new Token(TokenType.LEFT_PARENTHESES, range),
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    new Token(TokenType.RIGHT_PARENTHESES, range),
                    new Token(TokenType.LEFT_CURLY_BRACKET, range),
                    List.of(),
                    new Token(TokenType.RIGHT_CURLY_BRACKET, range));
        }

        Token openParen = advance(TokenType.LEFT_PARENTHESES);

        if (!isPossibleType()) {
            addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code));
            Token closeParen = current.is(TokenType.RIGHT_PARENTHESES) ? advance(TokenType.RIGHT_PARENTHESES) : null;
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ExtensionNode(
                    keyword,
                    openParen,
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    closeParen != null ? closeParen : new Token(TokenType.RIGHT_PARENTHESES, range),
                    new Token(TokenType.LEFT_CURLY_BRACKET, range),
                    List.of(),
                    new Token(TokenType.RIGHT_CURLY_BRACKET, range));
        }

        TypeNode typeNode = parseTypeNode();

        if (current.isNot(TokenType.RIGHT_PARENTHESES)) {
            addDiagnostic(ParserErrors.RightParenthesisExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ExtensionNode(
                    keyword,
                    openParen,
                    typeNode,
                    new Token(TokenType.RIGHT_PARENTHESES, range),
                    new Token(TokenType.LEFT_CURLY_BRACKET, range),
                    List.of(),
                    new Token(TokenType.RIGHT_CURLY_BRACKET, range));
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        if (current.isNot(TokenType.LEFT_CURLY_BRACKET)) {
            addDiagnostic(ParserErrors.OpenCurlyBracketExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ExtensionNode(
                    keyword,
                    openParen,
                    typeNode,
                    closeParen,
                    new Token(TokenType.LEFT_CURLY_BRACKET, range),
                    List.of(),
                    new Token(TokenType.RIGHT_CURLY_BRACKET, range));
        }

        Token openBrace = advance(TokenType.LEFT_CURLY_BRACKET);

        List<ClassMemberNode> members = new ArrayList<>();
        while (true) {
            if (current.is(TokenType.END_OF_FILE)) {
                break;
            }
            if (current.is(TokenType.RIGHT_CURLY_BRACKET)) {
                break;
            }
            if (isPossibleClassMemberNode()) {
                ClassMemberNode member = parseClassMemberNode();
                if (member.is(ParserNodeType.CLASS_METHOD) || member.is(ParserNodeType.CLASS_OPERATOR_OVERLOAD)) {
                    members.add(member);
                } else {
                    addDiagnostic(ParserErrors.ExtensionOnlyMethodsAllowed, member);
                }
            } else {
                Token token = advance();
                addDiagnostic(ParserErrors.ExtensionMethodExpected, token, token.getRawValue(code));
            }
        }

        Token closeBrace = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new ExtensionNode(keyword, openParen, typeNode, closeParen, openBrace, members, closeBrace);
    }

    private ClassNode parseClass() {
        Token keyword = advance(TokenType.CLASS);

        if (current.isNot(TokenType.IDENTIFIER)) {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", range);
            return new ClassNode(
                    keyword,
                    identifier,
                    new Token(TokenType.LEFT_CURLY_BRACKET, range),
                    List.of(),
                    new Token(TokenType.RIGHT_CURLY_BRACKET, range));
        }

        ValueToken identifier = (ValueToken) advance();

        Token colon = null;
        TypeNode baseTypeNode = null;
        if (current.is(TokenType.COLON)) {
            colon = advance(TokenType.COLON);
            if (isPossibleType()) {
                baseTypeNode = parseTypeNode();
            } else {
                addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code));
            }
        }

        Token openBrace = advance(TokenType.LEFT_CURLY_BRACKET);
        if (openBrace.getRange().getLength() == 0) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassNode(
                    keyword,
                    identifier,
                    colon,
                    baseTypeNode,
                    openBrace,
                    List.of(),
                    new Token(TokenType.RIGHT_SQUARE_BRACKET, range));
        }

        List<ClassMemberNode> members = new ArrayList<>();
        while (true) {
            if (current.is(TokenType.END_OF_FILE)) {
                break;
            }
            if (current.is(TokenType.RIGHT_CURLY_BRACKET)) {
                break;
            }
            if (isPossibleClassMemberNode()) {
                members.add(parseClassMemberNode());
            } else {
                Token token = advance();
                addDiagnostic(ParserErrors.ClassMemberExpected, token, token.getRawValue(code));
            }
        }

        Token closeBrace = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new ClassNode(keyword, identifier, colon, baseTypeNode, openBrace, members, closeBrace);
    }

    private boolean isPossibleClassMemberNode() {
        if (current.is(TokenType.VOID)) {
            return true;
        }
        if (isModifier(current.getTokenType())) {
            return true;
        }
        if (current.is(TokenType.CONSTRUCTOR)) {
            return true;
        }
        if (current.is(ContextualKeywords.OPERATOR)) {
            return true;
        }

        return isPossibleType();
    }

    private ClassMemberNode parseClassMemberNode() {
        if (current.is(TokenType.CONSTRUCTOR)) {
            return parseClassConstructor();
        }
        if (current.is(ContextualKeywords.OPERATOR)) {
            return parseClassOperatorOverload();
        }

        ModifiersNode modifiersNode = parseModifiers();
        if (current.is(TokenType.VOID) || modifiersNode.hasMethodModifiers()) {
            return parseClassMethod(modifiersNode);
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
            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRangeBeforeCurrent());
            Token semicolon = new Token(TokenType.SEMICOLON, createMissingTokenRangeBeforeCurrent());
            return new ClassFieldNode(typeNode, new NameExpressionNode(identifier), semicolon, TextRange.combine(typeNode, semicolon));
        }
    }

    private ClassConstructorNode parseClassConstructor() {
        if (current.isNot(TokenType.CONSTRUCTOR)) {
            throw new InternalException();
        }

        Token keyword = advance();
        ParameterListNode parameters = parseParameterList();

        Token colon = null;
        ConstructorInitializerNode initializer = null;
        Token arrow = null;

        StatementNode body;
        if (!parameters.isOpen()) {
            if (current.is(TokenType.COLON)) {
                colon = advance(TokenType.COLON);
                initializer = parseConstructorInitializer();
            }

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

        return new ClassConstructorNode(keyword, parameters, colon, initializer, arrow, body, TextRange.combine(keyword, body));
    }

    private ConstructorInitializerNode parseConstructorInitializer() {
        Token token;
        if (current.is(TokenType.BASE) || current.is(TokenType.THIS)) {
            token = advance();
        } else {
            addDiagnostic(ParserErrors.InvalidConstructorInitializer, current);
            token = createMissingTokenBeforeCurrent(TokenType.THIS);
        }

        ArgumentsListNode arguments = parseArgumentsList();
        return new ConstructorInitializerNode(token, arguments);
    }

    private ClassMethodNode parseClassMethod(ModifiersNode modifiersNode) {
        TypeNode typeNode = parseTypeOrVoidNode();

        ValueToken identifier;
        if (current.is(TokenType.IDENTIFIER)) {
            identifier = (ValueToken) advance();
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            identifier = createMissingIdentifierAfterLast();
        }

        return parseClassMethod(modifiersNode, typeNode, identifier);
    }

    private ClassMethodNode parseClassMethod(ModifiersNode modifiersNode, TypeNode typeNode, ValueToken identifier) {
        ParameterListNode parameters = parseParameterList();

        Token arrow = null;

        StatementNode body;
        if (!parameters.isOpen()) {
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
                body);
    }

    private ClassOperatorOverloadNode parseClassOperatorOverload() {
        ValueToken keyword = (ValueToken) advance(TokenType.IDENTIFIER);

        if (current.isNot(TokenType.LEFT_SQUARE_BRACKET)) {
            addDiagnostic(ParserErrors.InvalidOperatorDeclaration, current);
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassOperatorOverloadNode(
                    keyword,
                    new Token(TokenType.LEFT_SQUARE_BRACKET, range),
                    new Token(TokenType.PLUS, range),
                    new Token(TokenType.RIGHT_SQUARE_BRACKET, range),
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    createMissingParameterList(range),
                    null,
                    createMissingBlockStatement(range));
        }

        Token openBracket = advance();

        if (UnaryOperator.fromToken(current.getTokenType()) == null && BinaryOperator.fromToken(current.getTokenType()) == null) {
            addDiagnostic(ParserErrors.InvalidOperatorDeclaration, current);
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassOperatorOverloadNode(
                    keyword,
                    openBracket,
                    new Token(TokenType.PLUS, range),
                    new Token(TokenType.RIGHT_SQUARE_BRACKET, range),
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    createMissingParameterList(range),
                    null,
                    createMissingBlockStatement(range));
        }

        Token operatorToken = advance();

        if (current.isNot(TokenType.RIGHT_SQUARE_BRACKET)) {
            addDiagnostic(ParserErrors.InvalidOperatorDeclaration, current);
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassOperatorOverloadNode(
                    keyword,
                    openBracket,
                    operatorToken,
                    new Token(TokenType.RIGHT_SQUARE_BRACKET, range),
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    createMissingParameterList(range),
                    null,
                    createMissingBlockStatement(range));
        }

        Token closeBracket = advance();

        if (!isPossibleType()) {
            addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassOperatorOverloadNode(
                    keyword,
                    openBracket,
                    operatorToken,
                    closeBracket,
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    createMissingParameterList(range),
                    null,
                    createMissingBlockStatement(range));
        }

        TypeNode returnTypeNode = parseTypeNode();
        if (returnTypeNode.isOpen()) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassOperatorOverloadNode(
                    keyword,
                    openBracket,
                    operatorToken,
                    closeBracket,
                    returnTypeNode,
                    createMissingParameterList(range),
                    null,
                    createMissingBlockStatement(range));
        }

        ParameterListNode parametersNode = parseParameterList();
        if (parametersNode.isOpen()) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new ClassOperatorOverloadNode(
                    keyword,
                    openBracket,
                    operatorToken,
                    closeBracket,
                    returnTypeNode,
                    parametersNode,
                    null,
                    createMissingBlockStatement(range));
        }

        Token arrow;
        StatementNode body;

        if (parametersNode.isOpen()) {
            arrow = null;
            body = createMissingBlockStatement();
        } else {
            if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
                arrow = null;
                body = parseBlockStatement();
            } else if (current.is(TokenType.EQUAL_GREATER)) {
                arrow = advance(TokenType.EQUAL_GREATER);
                body = withSemicolon(parseSimpleStatementNotDeclaration(true));
            } else {
                arrow = null;
                body = createMissingBlockStatement();
                addDiagnostic(ParserErrors.CurlyBracketOrArrowExpected, current, current.getRawValue(code));
            }
        }

        return new ClassOperatorOverloadNode(
                keyword,
                openBracket,
                operatorToken,
                closeBracket,
                returnTypeNode,
                parametersNode,
                arrow,
                body);
    }

    private TypeAliasNode parseTypeAlias() {
        Token keyword = advance(TokenType.TYPEALIAS);

        if (current.isNot(TokenType.IDENTIFIER)) {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new TypeAliasNode(
                    keyword,
                    new ValueToken(TokenType.IDENTIFIER, "", range),
                    new Token(TokenType.EQUAL, range),
                    new InvalidTypeNode(new ValueToken(TokenType.IDENTIFIER, "", range)),
                    new Token(TokenType.SEMICOLON, range));
        }

        ValueToken identifier = (ValueToken) advance();

        if (current.isNot(TokenType.EQUAL)) {
            addDiagnostic(ParserErrors.EqualExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new TypeAliasNode(
                    keyword,
                    identifier,
                    new Token(TokenType.EQUAL, range),
                    new InvalidTypeNode(new ValueToken(TokenType.IDENTIFIER, "", range)),
                    new Token(TokenType.SEMICOLON, range));
        }

        Token equal = advance(TokenType.EQUAL);

        if (!isPossibleType()) {
            addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code));
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new TypeAliasNode(
                    keyword,
                    identifier,
                    equal,
                    new InvalidTypeNode(new ValueToken(TokenType.IDENTIFIER, "", range)),
                    new Token(TokenType.SEMICOLON, range));
        }

        TypeNode typeNode = parseTypeNode();
        Token semicolon = advance(TokenType.SEMICOLON);

        return new TypeAliasNode(keyword, identifier, equal, typeNode, semicolon);
    }

    private FunctionNode parseFunction() {
        ModifiersNode modifiers = parseModifiers();
        TypeNode returnType = parseTypeOrVoidNode();
        if (returnType.isMissing()) {
            TextRange missing = createMissingTokenRangeAfterLast();
            return new FunctionNode(
                    modifiers,
                    returnType,
                    createMissingNameExpression(missing),
                    createMissingParameterList(missing),
                    null,
                    createMissingInvalidStatement());
        }

        if (current.isNot(TokenType.IDENTIFIER)) {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            TextRange missing = createMissingTokenRangeAfterLast();
            return new FunctionNode(
                    modifiers,
                    returnType,
                    createMissingNameExpression(missing),
                    createMissingParameterList(missing),
                    null,
                    createMissingInvalidStatement());
        }

        ValueToken identifier = (ValueToken) advance();
        NameExpressionNode name = new NameExpressionNode(identifier);
        ParameterListNode parameters = parseParameterList();

        Token arrow = null;
        StatementNode statement;
        if (!parameters.isOpen()) {
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
        List<Token> modifiers = new ArrayList<>();
        while (true) {
            if (isModifier(current.getTokenType())) {
                if (modifiers.stream().anyMatch(token -> token.is(current.getTokenType()))) {
                    addDiagnostic(ParserErrors.DuplicateModifier, current, current.getRawValue(code));
                }
                modifiers.add(advance());
            } else {
                if (modifiers.isEmpty()) {
                    return new ModifiersNode(List.of(), createMissingTokenRangeBeforeCurrent());
                } else {
                    return new ModifiersNode(modifiers, TextRange.combine(modifiers));
                }
            }
        }
    }

    private boolean isModifier(TokenType type) {
        return switch (type) {
            case ASYNC, ABSTRACT, VIRTUAL, OVERRIDE -> true;
            default -> false;
        };
    }

    private ParameterListNode parseParameterList() {
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        if (openParen.getRange().getLength() == 0) {
            Token closeParen = createMissingTokenAfterLast(TokenType.RIGHT_PARENTHESES);
            return new ParameterListNode(
                    openParen,
                    SeparatedList.of(),
                    closeParen);
        }

        SeparatedList<ParameterNode> parameters = parseSeparatedList(
                this::parseParameterNode,
                () -> true,
                () -> addDiagnostic(ParserErrors.TypeOrCloseParenthesesExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code)),
                TokenType.RIGHT_PARENTHESES);

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);

        return new ParameterListNode(openParen, parameters, closeParen);
    }

    private ParameterNode parseParameterNode() {
        TypeNode type = parseRefTypeNode();

        ValueToken identifier;
        if (current.is(TokenType.IDENTIFIER)) {
            identifier = (ValueToken) advance();
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            identifier = createMissingIdentifier(createMissingTokenRangeBeforeCurrent());
        }

        NameExpressionNode name = new NameExpressionNode(identifier);
        return new ParameterNode(type, name);
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
                        return new VariableDeclarationNode(type, name, null, null, createMissingTokenBeforeCurrent(TokenType.SEMICOLON));
                    }
                    case EQUAL -> {
                        Token equal = advance(TokenType.EQUAL);
                        if (isPossibleExpression()) {
                            ExpressionNode expression = parseExpression();
                            return new VariableDeclarationNode(type, name, equal, expression, createMissingTokenBeforeCurrent(TokenType.SEMICOLON));
                        } else {
                            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                            ExpressionNode invalid = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
                            return new VariableDeclarationNode(type, name, equal, invalid, createMissingTokenBeforeCurrent(TokenType.SEMICOLON));
                        }
                    }
                    default -> {
                        addDiagnostic(ParserErrors.SemicolonOrEqualExpected, current, current.getRawValue(code));
                        return new VariableDeclarationNode(type, name, null, null, createMissingTokenBeforeCurrent(TokenType.SEMICOLON));
                    }
                }
            } else {
                addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code) + ".");

                ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRangeBeforeCurrent());
                NameExpressionNode name = new NameExpressionNode(identifier);
                return new VariableDeclarationNode(type, name, null, null, createMissingTokenBeforeCurrent(TokenType.SEMICOLON));
            }
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));

            ValueToken identifier = new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRangeBeforeCurrent());
            NameExpressionNode name = new NameExpressionNode(identifier);
            return new VariableDeclarationNode(type, name, null, null, createMissingTokenBeforeCurrent(TokenType.SEMICOLON));
        }
    }

    private StatementNode parseSimpleStatementNotDeclaration(boolean canBeExpression) {
        if (!canBeExpression && current.is(TokenType.THROW)) {
            Token keyword = advance();
            ExpressionNode expression = null;
            if (isPossibleExpression()) {
                expression = parseExpressionCore(Precedences.getThrow());
            }
            return new ThrowStatementNode(keyword, expression, null);
        }

        ExpressionNode expression1 = parseExpressionOrThrow();

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
                Token plusPlus = advance(TokenType.PLUS_PLUS);
                if (!canPostfix(expression1)) {
                    addDiagnostic(ParserErrors.CannotApplyIncDec, expression1);
                }
                return new PostfixStatementNode(ParserNodeType.INCREMENT_STATEMENT, expression1, plusPlus, null);
            } else if (current.is(TokenType.MINUS_MINUS)) {
                Token minusMinus = advance(TokenType.MINUS_MINUS);
                if (!canPostfix(expression1)) {
                    addDiagnostic(ParserErrors.CannotApplyIncDec, expression1);
                }
                return new PostfixStatementNode(ParserNodeType.DECREMENT_STATEMENT, expression1, minusMinus, null);
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
                expression2 = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
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
            case TRY:
                return true;

            default:
                return isPossibleExpression();
        }
    }

    private boolean isPossibleSimpleStatementOrDeclaration() {
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

    private boolean isPossibleSimpleStatementNotDeclaration() {
        return isPossibleSimpleStatementOrDeclaration() && !isPossibleDeclaration();
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
            case BOOLEAN, INT8, INT16, INT, INT32, INT64, LONG, FLOAT32, FLOAT, FLOAT64, STRING, CHAR, IDENTIFIER,
                 LEFT_PARENTHESES -> withSemicolon(parseSimpleStatementOrDeclaration());
            case LET -> withSemicolon(parseVariableDeclaration());
            case TRY -> parseTryStatement();
            default -> {
                if (isPossibleExpression()) {
                    yield withSemicolon(parseSimpleStatementOrDeclaration());
                } else {
                    throw new InternalException("Cannot parse statement.");
                }
            }
        };
    }

    private ExpressionNode parseExpressionOrThrow() {
        if (current.is(TokenType.THROW)) {
            Token keyword = advance();
            ExpressionNode expression = parseExpressionCore(Precedences.getThrow());
            return new ThrowExpressionNode(keyword, expression);
        } else {
            return parseExpression();
        }
    }

    private ExpressionNode parseExpression() {
        return parseExpressionCore(0);
    }

    private ExpressionNode parseExpressionCore(int precedence) {
        ExpressionNode left;

        if (current.is(TokenType.AWAIT)) {
            Token keyword = advance();
            ExpressionNode expression = parseExpressionCore(Precedences.getAwait());
            left = new AwaitExpressionNode(keyword, expression, TextRange.combine(keyword, expression));
        } else {
            UnaryOperator unary = UnaryOperator.fromToken(current.getTokenType());
            if (unary != null) {
                Token unaryToken = advance();
                ExpressionNode expression = parseExpressionCore(Precedences.get(unary));
                left = new UnaryExpressionNode(new UnaryOperatorNode(unaryToken, unary), expression);
            } else {
                left = parseTerm(precedence);
            }
        }

        return parseExpressionContinued(left, precedence);
    }

    private ExpressionNode parseExpressionContinued(ExpressionNode left, int precedence) {
        while (true) {
            BinaryOperator binary = BinaryOperator.fromToken(current.getTokenType());
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
                PatternNode patternNode = parsePatternNode();
                left = new IsExpressionNode(left, binaryToken, patternNode);
            } else if (binaryToken.is(TokenType.AS)) {
                TypeNode typeNode = parseTypeNode();
                left = new TypeCastExpressionNode(left, binaryToken, typeNode);
            } else if (isPossibleExpression()) {
                ExpressionNode right = parseExpressionCore(newPrecedence);
                left = new BinaryExpressionNode(
                        left,
                        new BinaryOperatorNode(binaryToken, binary),
                        right);
            } else {
                left = new BinaryExpressionNode(
                        left,
                        new BinaryOperatorNode(binaryToken, binary),
                        new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent()));
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                break;
            }
        }

        if (current.is(TokenType.QUESTION) && precedence <= Precedences.getConditionalExpression()) {
            Token question = advance(TokenType.QUESTION);
            ExpressionNode whenTrue = parseExpressionOrThrow();
            Token colon = advance(TokenType.COLON);
            ExpressionNode whenFalse = parseExpressionOrThrow();
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
                        index = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
                    }

                    Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);
                    expression = new IndexExpressionNode(expression, openBracket, index, closeBracket);
                }

                case DOT, DOT_HASH -> {
                    Token operator = advance();
                    if (current.is(TokenType.IDENTIFIER)) {
                        ValueToken identifier = (ValueToken) current;
                        NameExpressionNode name = new NameExpressionNode(identifier);
                        advance();
                        expression = new MemberAccessExpressionNode(expression, operator, name);
                    } else {
                        addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                        return new MemberAccessExpressionNode(expression, operator, createMissingNameExpressionAfterLast());
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
                return new RefArgumentExpressionNode(ref, name);
            } else {
                addDiagnostic(ParserErrors.InvalidRefExpression, current);
                NameExpressionNode name = createMissingNameExpressionAfterLast();
                return new RefArgumentExpressionNode(ref, name);
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
                    SeparatedList.of(),
                    closeParen);
        }

        SeparatedList<ExpressionNode> list = parseSeparatedList(
                this::parseArgumentExpression,
                this::isPossibleArgumentExpression,
                () -> addDiagnostic(ParserErrors.ExpressionOrCloseParenthesesExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code)),
                TokenType.RIGHT_PARENTHESES);

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new ArgumentsListNode(openParen, list, closeParen);
    }

    private ExpressionNode parseTermWithoutPostfix(int precedence) {
        ExpressionNode expression = switch (current.getTokenType()) {
            case IDENTIFIER -> {
                if (((ValueToken) current).value.equals("Java")) {
                    yield parseStaticReference();
                }
                yield isPossibleLambdaExpression() ? parseLambdaExpression() : new NameExpressionNode((ValueToken) advance());
            }
            case NULL -> new NullExpressionNode(advance());
            case FALSE -> new BooleanLiteralExpressionNode(advance(), false);
            case TRUE -> new BooleanLiteralExpressionNode(advance(), true);
            case INTEGER_LITERAL -> new IntegerLiteralExpressionNode(null, (ValueToken) advance());
            case INTEGER64_LITERAL -> new Integer64LiteralExpressionNode(null, (ValueToken) advance());
            case FLOAT_LITERAL -> new FloatLiteralExpressionNode((ValueToken) advance());
            case STRING_LITERAL -> new StringLiteralExpressionNode((ValueToken) advance());
            case CHAR_LITERAL -> new CharLiteralExpressionNode((ValueToken) advance());
            case NEW -> parseNewExpression();
            case LEFT_PARENTHESES -> isPossibleLambdaExpression() ? parseLambdaExpression() : parseParenthesizedExpression();
            case LEFT_SQUARE_BRACKET -> parseCollectionExpression();
            case BOOLEAN, INT8, INT16, INT, INT32, INT64, LONG, CHAR, FLOAT32, FLOAT, FLOAT64, STRING -> parseStaticReference();
            case META_UNKNOWN -> new InvalidMetaExpressionNode(advance());
            case META_CAST -> parseMetaCastExpression();
            case META_TYPE -> parseMetaTypeExpression();
            case META_TYPE_OF -> parseMetaTypeOfExpression();
            case THIS -> new ThisExpressionNode(advance());
            case BASE -> new BaseExpressionNode(advance());
            default -> null;
        };

        if (expression != null) {
            return expression;
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            return new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
        }
    }

    private ExpressionNode parseNewExpression() {
        Token keyword = advance(TokenType.NEW);

        if (!isPossibleType()) {
            addDiagnostic(ParserErrors.OpenNewExpression, keyword);
            return new InvalidExpressionNode(List.of(keyword));
        }

        TypeNode typeNode = parseTypeNode();

        if (current.is(TokenType.LEFT_SQUARE_BRACKET)) {
            return parseArrayCreationExpression(keyword, typeNode);
        } else if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
            return parseArrayInitializerExpression(keyword, typeNode);
        } else if (current.is(TokenType.LEFT_PARENTHESES)) {
            return parseObjectCreationExpression(keyword, typeNode);
        } else {
            addDiagnostic(ParserErrors.InvalidNewExpression, current);
            return new InvalidExpressionNode(List.of(keyword, typeNode));
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
            lengthExpression = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
        }

        Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);

        return new ArrayCreationExpressionNode(
                newToken,
                typeNode,
                openBracket,
                lengthExpression,
                closeBracket);
    }

    private ExpressionNode parseArrayInitializerExpression(Token newToken, TypeNode typeNode) {
        if (current.isNot(TokenType.LEFT_CURLY_BRACKET)) {
            throw new InternalException();
        }

        Token openBrace = advance(TokenType.LEFT_CURLY_BRACKET);
        SeparatedList<ExpressionNode> list = parseSeparatedList(
                this::parseExpression,
                this::isPossibleExpression,
                () -> addDiagnostic(ParserErrors.ExpressionOrCloseBraceExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.CommaOrCloseCurlyBracketExpected, current, current.getRawValue(code)),
                TokenType.RIGHT_CURLY_BRACKET);
        Token closeBrace = advance(TokenType.RIGHT_CURLY_BRACKET);

        return new ArrayInitializerExpressionNode(newToken, typeNode, openBrace, list, closeBrace, TextRange.combine(newToken, last));
    }

    private ExpressionNode parseObjectCreationExpression(Token keyword, TypeNode typeNode) {
        if (current.isNot(TokenType.LEFT_PARENTHESES)) {
            throw new InternalException();
        }

        ArgumentsListNode arguments = parseArgumentsList();
        return new ObjectCreationExpressionNode(keyword, typeNode, arguments);
    }

    private ExpressionNode parseParenthesizedExpression() {
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        ExpressionNode expression = parseExpression();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new ParenthesizedExpressionNode(openParen, expression, closeParen);
    }

    private ExpressionNode parseCollectionExpression() {
        Token openBracket = advance(TokenType.LEFT_SQUARE_BRACKET);
        SeparatedList<ExpressionNode> list = parseSeparatedList(
                this::parseExpression,
                this::isPossibleExpression,
                () -> addDiagnostic(ParserErrors.ExpressionOrCloseSquaredBracketExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code)),
                () -> addDiagnostic(ParserErrors.CommaOrCloseSquareBracketExpected, current, current.getRawValue(code)),
                TokenType.RIGHT_SQUARE_BRACKET);
        Token closeBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);

        return new CollectionExpressionNode(openBracket, list, closeBracket, TextRange.combine(openBracket, closeBracket));
    }

    private LambdaExpressionNode parseLambdaExpression() {
        Token openParen;
        SeparatedList<NameExpressionNode> parameters;
        Token closeParen;

        if (current.is(TokenType.IDENTIFIER)) {
            openParen = createMissingTokenBeforeCurrent(TokenType.LEFT_PARENTHESES);
            parameters = new SeparatedList<>();
            parameters.add(new NameExpressionNode((ValueToken) advance(TokenType.IDENTIFIER)));
            closeParen = createMissingValueTokenAfterLast(TokenType.RIGHT_PARENTHESES);
        } else if (current.is(TokenType.LEFT_PARENTHESES)) {
            openParen = advance(TokenType.LEFT_PARENTHESES);
            parameters = parseSeparatedList(
                    () -> new NameExpressionNode((ValueToken) advance(TokenType.IDENTIFIER)),
                    () -> current.is(TokenType.IDENTIFIER),
                    () -> addDiagnostic(ParserErrors.IdentifierOrCloseParenthesesExpected, current, current.getRawValue(code)),
                    () -> addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code)),
                    () -> addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code)),
                    TokenType.RIGHT_PARENTHESES);
            closeParen = advance(TokenType.RIGHT_PARENTHESES);
        } else {
            throw new InternalException("Check isPossibleLambdaExpression() method.");
        }

        Token arrow = advance(TokenType.EQUAL_GREATER);

        StatementNode statement;
        if (current.is(TokenType.LEFT_CURLY_BRACKET)) {
            statement = parseBlockStatement();
        } else {
            if (isPossibleSimpleStatementNotDeclaration()) {
                statement = parseSimpleStatementNotDeclaration(true);
            } else {
                statement = new InvalidStatementNode(createMissingTokenRangeBeforeCurrent());
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
            }
        }

        return new LambdaExpressionNode(openParen, parameters, closeParen, arrow, statement);
    }

    private StaticReferenceNode parseStaticReference() {
        TypeNode typeNode = parseTypeNode();
        return new StaticReferenceNode(typeNode, typeNode.getRange());
    }

    private MetaCastExpressionNode parseMetaCastExpression() {
        Token keyword = advance(TokenType.META_CAST);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        if (openParen.isMissing()) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new MetaCastExpressionNode(
                    keyword,
                    openParen,
                    new InvalidExpressionNode(range),
                    new Token(TokenType.COMMA, range),
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    new Token(TokenType.RIGHT_PARENTHESES, range));
        }

        ExpressionNode expression;
        if (isPossibleExpression()) {
            expression = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            expression = new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent());
        }

        Token comma = advance(TokenType.COMMA);
        if (comma.isMissing()) {
            TextRange range = createMissingTokenRangeBeforeCurrent();
            return new MetaCastExpressionNode(
                    keyword,
                    openParen,
                    expression,
                    comma,
                    new InvalidTypeNode(createMissingIdentifier(range)),
                    new Token(TokenType.RIGHT_PARENTHESES, range));
        }

        TypeNode typeNode;
        if (isPossibleType()) {
            typeNode = parseTypeNode();
        } else {
            addDiagnostic(ParserErrors.TypeExpected, current, current.getRawValue(code));
            typeNode = new InvalidTypeNode(createMissingIdentifier(createMissingTokenRangeBeforeCurrent()));
        }

        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new MetaCastExpressionNode(keyword, openParen, expression, comma, typeNode, closeParen);
    }

    private MetaTypeExpressionNode parseMetaTypeExpression() {
        Token keyword = advance(TokenType.META_TYPE);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        TypeNode type = parseTypeNode();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new MetaTypeExpressionNode(keyword, openParen, type, closeParen);
    }

    private MetaTypeOfExpressionNode parseMetaTypeOfExpression() {
        Token keyword = advance(TokenType.META_TYPE_OF);
        Token openParen = advance(TokenType.LEFT_PARENTHESES);
        ExpressionNode expression = parseExpression();
        Token closeParen = advance(TokenType.RIGHT_PARENTHESES);
        return new MetaTypeOfExpressionNode(keyword, openParen, expression, closeParen);
    }

    private boolean isPossibleExpression() {
        return switch (current.getTokenType()) {
            case NULL, FALSE, TRUE, LEFT_PARENTHESES, LEFT_SQUARE_BRACKET, INTEGER_LITERAL, INTEGER64_LITERAL,
                 FLOAT_LITERAL, STRING_LITERAL, CHAR_LITERAL, NEW, IDENTIFIER, BOOLEAN, INT8, INT16, INT, INT32, INT64,
                 LONG, FLOAT32, FLOAT, FLOAT64, STRING, CHAR, PLUS, MINUS, EXCLAMATION, AWAIT, META_UNKNOWN, META_CAST,
                 META_TYPE, META_TYPE_OF, THIS, BASE, THROW -> true;
            default -> false;
        };
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

    private PatternNode parsePatternNode() {
        if (current.is(TokenType.IDENTIFIER) && current instanceof ValueToken value && value.value.equals("not")) {
            advance();
            return new NotPatternNode(value, parsePatternNode());
        }

        if (isPossibleType()) {
            TypeNode typeNode = parseTypeNode();
            if (current.is(TokenType.IDENTIFIER)) {
                // <type> <identifier>
                ValueToken identifier = (ValueToken) advance();
                return new DeclarationPatternNode(typeNode, identifier);
            } else {
                // <type> <not identifier>
                return new TypePatternNode(typeNode);
            }
        }

        if (isPossibleExpression()) {
            ExpressionNode expression = parseExpression();
            return new ConstantPatternNode(expression);
        }

        addDiagnostic(ParserErrors.PatternExpected, current, current.getRawValue(code));
        return new ConstantPatternNode(new InvalidExpressionNode(createMissingTokenRangeBeforeCurrent()));
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
            return new RefTypeNode(ref, underlying);
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseLetTypeNode() {
        if (current.is(TokenType.LET)) {
            return new LetTypeNode(advance(TokenType.LET));
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseTypeOrVoidNode() {
        if (current.is(TokenType.VOID)) {
            return new VoidTypeNode(advance());
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseTypeNode() {
        TypeNode type = switch (current.getTokenType()) {
            case BOOLEAN -> new PredefinedTypeNode(advance(), PredefinedType.BOOLEAN);
            case INT8 -> new PredefinedTypeNode(advance(), PredefinedType.INT8);
            case INT16 -> new PredefinedTypeNode(advance(), PredefinedType.INT16);
            case INT, INT32 -> new PredefinedTypeNode(advance(), PredefinedType.INT);
            case INT64, LONG -> new PredefinedTypeNode(advance(), PredefinedType.INT64);
            case FLOAT32 -> new PredefinedTypeNode(advance(), PredefinedType.FLOAT32);
            case FLOAT, FLOAT64 -> new PredefinedTypeNode(advance(), PredefinedType.FLOAT);
            case STRING -> new PredefinedTypeNode(advance(), PredefinedType.STRING);
            case CHAR -> new PredefinedTypeNode(advance(), PredefinedType.CHAR);
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
                    yield new InvalidTypeNode(createMissingIdentifierAfterLast());
                } else {
                    yield new InvalidTypeNode(advance());
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
        Token java = advance(TokenType.IDENTIFIER);
        Token openBracket = advance(TokenType.LESS);
        Token closeBracket = null;

        JavaQualifiedTypeNameNode qualifiedTypeName;
        if (!openBracket.getRange().isEmpty()) {
            qualifiedTypeName = parseQualifiedTypeName();
            closeBracket = advance(TokenType.GREATER);
        } else {
            qualifiedTypeName = new JavaQualifiedTypeNameNode(List.of(createMissingIdentifierAfterLast()), "");
        }

        if (closeBracket == null) {
            closeBracket = createMissingTokenBeforeCurrent(TokenType.GREATER);
        }

        return new JavaTypeNode(java, openBracket, qualifiedTypeName, closeBracket);
    }

    private JavaQualifiedTypeNameNode parseQualifiedTypeName() {
        final int STATE_START = 1;
        final int STATE_IDENTIFIER_READ = 2;
        final int STATE_SEPARATOR_READ = 3;
        final int STATE_END = 10;

        StringBuilder sb = new StringBuilder();
        List<Token> tokens = new ArrayList<>();

        int state = STATE_START;
        while (state != STATE_END) {
            switch (state) {
                case STATE_START, STATE_SEPARATOR_READ -> {
                    if (current.is(TokenType.IDENTIFIER)) {
                        ValueToken token = (ValueToken) advance();
                        sb.append(token.value);
                        tokens.add(token);
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
                        sb.append('.');
                        tokens.add(advance());
                        state = STATE_SEPARATOR_READ;
                    } else if (current.is(TokenType.DOLLAR)) {
                        sb.append('$');
                        tokens.add(advance());
                        state = STATE_SEPARATOR_READ;
                    } else {
                        // error will be added later
                        state = STATE_END;
                    }
                }
            }
        }

        if (tokens.isEmpty()) {
            tokens.add(createMissingIdentifierAfterLast());
        }

        return new JavaQualifiedTypeNameNode(tokens, sb.toString());
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
                    new InvalidTypeNode(createMissingIdentifier(missing)),
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
                    () -> addDiagnostic(ParserErrors.TypeOrCloseParenthesesExpected, current, current.getRawValue(code)),
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
                    new InvalidTypeNode(createMissingIdentifier(missing)),
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

    private boolean isPossibleType() {
        LookAhead ahead = new LookAhead();
        try {
            return tryAdvanceType();
        } finally {
            ahead.rollback();
        }
    }

    private boolean tryAdvanceTypeOrVoid() {
        if (current.is(TokenType.VOID)) {
            advance();
            return true;
        }

        return tryAdvanceType();
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

        if (!tryAdvanceTypeOrVoid()) {
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
            Runnable onCannotAdvanceFirst,
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
                        onCannotAdvanceFirst.run();
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
                case COMMA -> addDiagnostic(ParserErrors.CommaExpected, current, current.getRawValue(code));
                default -> throw new RuntimeException("Not implemented");
            }

            return switch (type) {
                case LEFT_PARENTHESES, SEMICOLON, GREATER, EQUAL_GREATER -> createMissingTokenAfterLast(type);
                default -> createMissingTokenBeforeCurrent(type);
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

    private NameExpressionNode createMissingNameExpressionAfterLast() {
        return new NameExpressionNode(createMissingIdentifierAfterLast());
    }

    private NameExpressionNode createMissingNameExpression(TextRange range) {
        return new NameExpressionNode(createMissingIdentifier(range));
    }

    private ParameterListNode createMissingParameterList(TextRange range) {
        return new ParameterListNode(
                new Token(TokenType.LEFT_PARENTHESES, range),
                SeparatedList.of(),
                new Token(TokenType.RIGHT_PARENTHESES, range));
    }

    private InvalidStatementNode createMissingInvalidStatement() {
        return new InvalidStatementNode(createMissingTokenRangeAfterLast());
    }

    private Token createMissingTokenAfterLast(TokenType type) {
        return new Token(type, createMissingTokenRangeAfterLast());
    }

    private Token createMissingTokenBeforeCurrent(TokenType type) {
        return new Token(type, createMissingTokenRangeBeforeCurrent());
    }

    private ValueToken createMissingIdentifier(TextRange range) {
        return new ValueToken(TokenType.IDENTIFIER, "", range);
    }

    private ValueToken createMissingIdentifierAfterLast() {
        return createMissingValueTokenAfterLast(TokenType.IDENTIFIER);
    }

    private ValueToken createMissingIdentifierBeforeCurrent() {
        return new ValueToken(TokenType.IDENTIFIER, "", createMissingTokenRangeBeforeCurrent());
    }

    private ValueToken createMissingValueTokenAfterLast(TokenType type) {
        return new ValueToken(type, "", createMissingTokenRangeAfterLast());
    }

    private TextRange createMissingTokenRangeAfterLast() {
        return new SingleLineTextRange(
                last.getRange().getLine2(),
                last.getRange().getColumn2(),
                last.getRange().getPosition() + last.getRange().getLength(),
                0);
    }

    private TextRange createMissingTokenRangeBeforeCurrent() {
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