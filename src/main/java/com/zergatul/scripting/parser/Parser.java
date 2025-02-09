package com.zergatul.scripting.parser;

import com.zergatul.scripting.*;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.nodes.*;

import java.util.ArrayList;
import java.util.List;

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
        while (current.type != TokenType.END_OF_FILE) {
            if (current.type == TokenType.STATIC) {
                members.add(parseStaticField());
            } else if (isPossibleFunction()) {
                members.add(parseFunction());
            } else {
                break;
            }
        }

        List<StatementNode> statements = new ArrayList<>();
        while (current.type != TokenType.END_OF_FILE) {
            if (isPossibleStatement()) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                advance();
            }
        }

        CompilationUnitMembersListNode membersList = new CompilationUnitMembersListNode(
                members,
                members.isEmpty() ?
                        new SingleLineTextRange(1, 1, 0, 0) :
                        TextRange.combine(members.get(0), members.get(members.size() - 1)));

        StatementsListNode statementsList = new StatementsListNode(
                statements,
                statements.isEmpty() ?
                        new SingleLineTextRange(
                                membersList.getRange().getLine2(),
                                membersList.getRange().getColumn2(),
                                membersList.getRange().getPosition() + membersList.getRange().getLength(),
                                0) :
                        TextRange.combine(statements.get(0), statements.get(statements.size() - 1)));

        return new CompilationUnitNode(membersList, statementsList, TextRange.combine(membersList, statementsList));
    }

    private BlockStatementNode parseBlockStatement() {
        Token first = advance(TokenType.LEFT_CURLY_BRACKET);

        List<StatementNode> statements = new ArrayList<>();
        while (current.type != TokenType.RIGHT_CURLY_BRACKET && current.type != TokenType.END_OF_FILE) {
            if (isPossibleStatement()) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                advance();
            }
        }

        Token last = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new BlockStatementNode(statements, TextRange.combine(first, last));
    }

    private IfStatementNode parseIfStatement() {
        Token ifToken = advance(TokenType.IF);

        Token lParen = advance(TokenType.LEFT_PARENTHESES);

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRange());
        }

        Token rParen = advance(TokenType.RIGHT_PARENTHESES);

        StatementNode thenStatement;
        if (isPossibleStatement()) {
            thenStatement = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            thenStatement = new InvalidStatementNode(createMissingTokenRange());
        }

        StatementNode elseStatement = null;
        if (current.type == TokenType.ELSE) {
            advance();
            if (isPossibleStatement()) {
                elseStatement = parseStatement();
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                elseStatement = new InvalidStatementNode(createMissingTokenRange());
            }
        }

        return new IfStatementNode(lParen, rParen, condition, thenStatement, elseStatement, TextRange.combine(ifToken, elseStatement == null ? thenStatement : elseStatement));
    }

    private ReturnStatementNode parseReturnStatement() {
        Token returnToken = advance(TokenType.RETURN);
        ExpressionNode expression = null;
        if (isPossibleExpression()) {
            expression = parseExpression();
        }

        Token semicolon = advance(TokenType.SEMICOLON);
        return new ReturnStatementNode(expression, TextRange.combine(returnToken, semicolon));
    }

    private ForLoopStatementNode parseForLoopStatement() {
        Token forToken = advance(TokenType.FOR);
        advance(TokenType.LEFT_PARENTHESES);

        StatementNode init;
        if (current.type == TokenType.SEMICOLON) {
            init = new EmptyStatementNode(current.getRange());
            advance();
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
        if (current.type == TokenType.SEMICOLON) {
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
        if (current.type == TokenType.RIGHT_PARENTHESES) {
            update = new EmptyStatementNode(current.getRange());
        } else {
            if (isPossibleSimpleStatement() && !isPossibleDeclaration()) {
                update = parseSimpleStatementNotDeclaration();
            } else {
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
                update = new InvalidStatementNode(createMissingTokenRange());
            }
        }

        advance(TokenType.RIGHT_PARENTHESES);
        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRange());
        }

        return new ForLoopStatementNode(init, condition, update, body, TextRange.combine(forToken, body));
    }

    private ForEachLoopStatementNode parseForEachLoopStatement() {
        Token foreachToken = advance(TokenType.FOREACH);
        advance(TokenType.LEFT_PARENTHESES);

        TypeNode typeNode;
        if (isPossibleDeclaration()) {
            typeNode = parseLetTypeNode();
        } else {
            addDiagnostic(ParserErrors.ForEachTypeIdentifierRequired, current);
            typeNode = new InvalidTypeNode(createMissingTokenRange());
        }

        IdentifierToken identifier;
        if (current.type == TokenType.IDENTIFIER) {
            identifier = (IdentifierToken) advance();
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            identifier = new IdentifierToken("", createMissingTokenRange());
        }

        advance(TokenType.IN);

        ExpressionNode iterable;
        if (isPossibleExpression()) {
            iterable = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            iterable = new InvalidExpressionNode(createMissingTokenRange());
        }

        advance(TokenType.RIGHT_PARENTHESES);

        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRange());
        }

        return new ForEachLoopStatementNode(
                typeNode,
                new NameExpressionNode(identifier),
                iterable,
                body,
                TextRange.combine(foreachToken, body));
    }

    private WhileLoopStatementNode parseWhileLoopStatement() {
        Token whileToken = advance(TokenType.WHILE);

        advance(TokenType.LEFT_PARENTHESES);

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRange());
        }

        advance(TokenType.RIGHT_PARENTHESES);

        StatementNode body;
        if (isPossibleStatement()) {
            body = parseStatement();
        } else {
            addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            body = new InvalidStatementNode(createMissingTokenRange());
        }

        return new WhileLoopStatementNode(condition, body, TextRange.combine(whileToken, body));
    }

    private BreakStatementNode parseBreakStatement() {
        Token breakToken = advance(TokenType.BREAK);
        Token semicolon = advance(TokenType.SEMICOLON);
        return new BreakStatementNode(TextRange.combine(breakToken, semicolon));
    }

    private ContinueStatementNode parseContinueStatement() {
        Token continueToken = advance(TokenType.CONTINUE);
        Token semicolon = advance(TokenType.SEMICOLON);
        return new ContinueStatementNode(TextRange.combine(continueToken, semicolon));
    }

    private EmptyStatementNode parseEmptyStatement() {
        Token semicolon = advance(TokenType.SEMICOLON);
        return new EmptyStatementNode(semicolon.getRange());
    }

    private StatementNode parseSimpleStatement() {
        if (isPossibleDeclaration()) {
            return parseVariableDeclaration();
        } else {
            return parseSimpleStatementNotDeclaration();
        }
    }

    private boolean isPossibleDeclaration() {
        if (current.type == TokenType.LET) {
            return true;
        }

        Token next = peek(1);
        // "int." is not declaration
        if (isPredefinedType() && next.type != TokenType.DOT) {
            return true;
        }

        if (current.type == TokenType.IDENTIFIER) {
            if (next.type == TokenType.LEFT_SQUARE_BRACKET && peek(2).type == TokenType.RIGHT_SQUARE_BRACKET) {
                return true;
            }
            if (next.type == TokenType.IDENTIFIER) {
                return true;
            }
        }

        return false;
    }

    private boolean isPossibleFunction() {
        if (current.type == TokenType.ASYNC) {
            return true;
        }
        if (current.type == TokenType.VOID) {
            return true;
        }

        // "int." definitely not a function
        if (isPredefinedType() && peek(1).type == TokenType.DOT) {
            return false;
        }

        int cursor = 1;
        if (isPredefinedType() || current.type == TokenType.IDENTIFIER) {
            // process possible []
            while (peek(cursor).type == TokenType.LEFT_SQUARE_BRACKET && peek(cursor + 1).type == TokenType.RIGHT_SQUARE_BRACKET) {
                cursor += 2;
            }

            return peek(cursor).type == TokenType.IDENTIFIER && peek(cursor + 1).type == TokenType.LEFT_PARENTHESES;
        } else {
            return false;
        }
    }

    private StaticFieldNode parseStaticField() {
        Token staticToken = advance(TokenType.STATIC);
        VariableDeclarationNode declaration = parseVariableDeclaration();
        Token semicolon = advance(TokenType.SEMICOLON);
        return new StaticFieldNode(declaration, TextRange.combine(staticToken, semicolon));
    }

    private FunctionNode parseFunction() {
        Token asyncToken = null;
        if (current.type == TokenType.ASYNC) {
            asyncToken = advance();
        }

        TypeNode returnType;
        if (current.type == TokenType.VOID) {
            returnType = new VoidTypeNode(advance().getRange());
        } else {
            returnType = parseTypeNode();
        }

        IdentifierToken identifier;
        if (current.type == TokenType.IDENTIFIER) {
            identifier = (IdentifierToken) advance();
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            identifier = new IdentifierToken("", createMissingTokenRange());
        }

        NameExpressionNode name = new NameExpressionNode(identifier);
        ParameterListNode parameters = parseParameterList();
        BlockStatementNode body = parseBlockStatement();

        TextRange range = TextRange.combine(asyncToken != null ? asyncToken : returnType, body);
        return new FunctionNode(asyncToken, returnType, name, parameters, body, range);
    }

    private ParameterListNode parseParameterList() {
        Token begin = advance(TokenType.LEFT_PARENTHESES);
        Token end;

        List<ParameterNode> parameters = new ArrayList<>();

        if (current.type == TokenType.RIGHT_PARENTHESES) {
            end = advance();
        } else {
            while (true) {
                TypeNode type = parseRefTypeNode();
                IdentifierToken identifier;
                if (current.type == TokenType.IDENTIFIER) {
                    identifier = (IdentifierToken) advance();
                } else {
                    addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                    identifier = new IdentifierToken("", createMissingTokenRange());
                }

                NameExpressionNode name = new NameExpressionNode(identifier);
                parameters.add(new ParameterNode(type, name, TextRange.combine(type, name)));

                if (current.type == TokenType.RIGHT_PARENTHESES) {
                    end = advance();
                    break;
                } else if (current.type == TokenType.COMMA) {
                    advance();
                } else {
                    addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                    end = createMissingToken(TokenType.LEFT_PARENTHESES);
                    break;
                }
            }
        }

        return new ParameterListNode(parameters, TextRange.combine(begin, end));
    }

    private VariableDeclarationNode parseVariableDeclaration() {
        TypeNode type = parseLetTypeNode();
        if (current.type == TokenType.IDENTIFIER) {
            IdentifierToken identifier = (IdentifierToken) advance();
            NameExpressionNode name = new NameExpressionNode(identifier);
            switch (current.type) {
                case SEMICOLON -> {
                    if (type.getNodeType() == NodeType.LET_TYPE) {
                        addDiagnostic(ParserErrors.CannotUseLet, type.getRange());
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
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));

            IdentifierToken identifier = new IdentifierToken("", createMissingTokenRange());
            NameExpressionNode name = new NameExpressionNode(identifier);
            return new VariableDeclarationNode(type, name, TextRange.combine(type, name));
        }
    }

    private StatementNode parseSimpleStatementNotDeclaration() {
        // TODO: custom types
        ExpressionNode expression1 = parseExpression();
        AssignmentOperator assignment = switch (current.type) {
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
            if (current.type == TokenType.PLUS_PLUS) {
                Token plusPlus = advance();
                if (!canPostfix(expression1)) {
                    addDiagnostic(ParserErrors.CannotApplyIncDec, expression1);
                }
                return new PostfixStatementNode(NodeType.INCREMENT_STATEMENT, expression1, TextRange.combine(expression1, plusPlus));
            } else if (current.type == TokenType.MINUS_MINUS) {
                Token minusMinus = advance();
                if (!canPostfix(expression1)) {
                    addDiagnostic(ParserErrors.CannotApplyIncDec, expression1);
                }
                return new PostfixStatementNode(NodeType.DECREMENT_STATEMENT, expression1, TextRange.combine(expression1, minusMinus));
            } else {
                return new ExpressionStatementNode(expression1, expression1.getRange());
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
                    new AssignmentOperatorNode(assignment, assignmentToken.getRange()),
                    expression2,
                    TextRange.combine(expression1, expression2));
        }
    }

    private boolean canPostfix(ExpressionNode expression) {
        NodeType type = expression.getNodeType();
        return type == NodeType.NAME_EXPRESSION || type == NodeType.INDEX_EXPRESSION || type == NodeType.MEMBER_ACCESS_EXPRESSION;
    }

    private boolean isPossibleStatement() {
        switch (current.type) {
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
        switch (current.type) {
            case BOOLEAN:
            case INT:
            case INT32:
            case INT64:
            case LONG:
            case FLOAT:
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
        return switch (current.type) {
            case LEFT_CURLY_BRACKET -> parseBlockStatement();
            case IF -> parseIfStatement();
            case RETURN -> parseReturnStatement();
            case FOR -> parseForLoopStatement();
            case FOREACH -> parseForEachLoopStatement();
            case WHILE -> parseWhileLoopStatement();
            case BREAK -> parseBreakStatement();
            case CONTINUE -> parseContinueStatement();
            case SEMICOLON -> parseEmptyStatement();
            case BOOLEAN, INT, INT32, INT64, LONG, FLOAT, STRING, CHAR, IDENTIFIER, LEFT_PARENTHESES -> parseSimpleStatement().append(advance(TokenType.SEMICOLON));
            case LET -> parseVariableDeclaration().append(advance(TokenType.SEMICOLON));
            default -> {
                if (isPossibleExpression()) {
                    yield parseSimpleStatement().append(advance(TokenType.SEMICOLON));
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

        if (current.type == TokenType.AWAIT) {
            Token awaitToken = advance();
            ExpressionNode expression = parseExpressionCore(Precedences.getAwait());
            left = new AwaitExpressionNode(awaitToken, expression, TextRange.combine(awaitToken, expression));
        } else {
            UnaryOperator unary = switch (current.type) {
                case PLUS -> UnaryOperator.PLUS;
                case MINUS -> UnaryOperator.MINUS;
                case EXCLAMATION -> UnaryOperator.NOT;
                default -> null;
            };

            if (unary != null) {
                Token unaryToken = advance();
                ExpressionNode expression = parseExpressionCore(Precedences.get(unary));
                if (unary == UnaryOperator.MINUS && expression instanceof IntegerLiteralExpressionNode integer && !integer.value.startsWith("-")) {
                    left = new IntegerLiteralExpressionNode("-" + integer.value, TextRange.combine(unaryToken, expression));
                } else if (unary == UnaryOperator.MINUS && expression instanceof Integer64LiteralExpressionNode integer && !integer.value.startsWith("-")) {
                    left = new Integer64LiteralExpressionNode("-" + integer.value, TextRange.combine(unaryToken, expression));
                } else if (unary == UnaryOperator.MINUS && expression instanceof FloatLiteralExpressionNode floatLiteral && !floatLiteral.value.startsWith("-")) {
                    left = new FloatLiteralExpressionNode("-" + floatLiteral.value, TextRange.combine(unaryToken, expression));
                } else {
                    left = new UnaryExpressionNode(
                            new UnaryOperatorNode(unary, unaryToken.getRange()),
                            expression,
                            TextRange.combine(unaryToken, expression));
                }
            } else {
                left = parseTerm(precedence);
            }
        }

        return parseExpressionContinued(left, precedence);
    }

    private ExpressionNode parseExpressionContinued(ExpressionNode left, int precedence) {
        while (true) {
            BinaryOperator binary = switch (current.type) {
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

            if (isPossibleExpression()) {
                ExpressionNode right = parseExpressionCore(newPrecedence);
                left = new BinaryExpressionNode(
                        left,
                        new BinaryOperatorNode(binary, binaryToken.getRange()),
                        right,
                        TextRange.combine(left, right));
            } else {
                left = new BinaryExpressionNode(
                        left,
                        new BinaryOperatorNode(binary, binaryToken.getRange()),
                        new InvalidExpressionNode(createMissingTokenRange()),
                        TextRange.combine(left, binaryToken));
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                break;
            }
        }

        if (current.type == TokenType.QUESTION && precedence <= Precedences.getConditionalExpression()) {
            advance(TokenType.QUESTION);
            ExpressionNode whenTrue = parseExpression();
            advance(TokenType.COLON);
            ExpressionNode whenFalse = parseExpression();
            return new ConditionalExpressionNode(left, whenTrue, whenFalse, TextRange.combine(left, whenFalse));
        } else {
            return left;
        }
    }

    private ExpressionNode parseTerm(int precedence) {
        return parsePostFixExpression(parseTermWithoutPostfix(precedence));
    }

    private ExpressionNode parsePostFixExpression(ExpressionNode expression) {
        while (true) {
            switch (current.type) {
                case LEFT_PARENTHESES -> {
                    ArgumentsListNode arguments = parseArgumentsList();
                    expression = new InvocationExpressionNode(expression, arguments, TextRange.combine(expression, arguments));
                }

                case LEFT_SQUARE_BRACKET -> {
                    advance(TokenType.LEFT_SQUARE_BRACKET);
                    ExpressionNode index;
                    if (isPossibleExpression()) {
                        index = parseExpression();
                    } else {
                        addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                        index = new InvalidExpressionNode(createMissingTokenRange());
                    }

                    Token right = advance(TokenType.RIGHT_SQUARE_BRACKET);
                    expression = new IndexExpressionNode(expression, index, TextRange.combine(expression, right));
                }

                case DOT -> {
                    Token dot = advance();
                    if (current.type == TokenType.IDENTIFIER) {
                        IdentifierToken identifier = (IdentifierToken) current;
                        NameExpressionNode name = new NameExpressionNode(identifier.value, identifier.getRange());
                        advance();
                        expression = new MemberAccessExpressionNode(expression, name, TextRange.combine(expression, name));
                    } else {
                        addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                        return new MemberAccessExpressionNode(expression, new NameExpressionNode("", createMissingTokenRangeAfterLast()), TextRange.combine(expression, dot));
                    }
                }

                default -> {
                    return expression;
                }
            }
        }
    }

    private ExpressionNode parseArgumentExpression() {
        if (current.type == TokenType.REF) {
            Token ref = advance();
            if (current.type == TokenType.IDENTIFIER) {
                NameExpressionNode name = new NameExpressionNode((IdentifierToken) advance());
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
        Token left = advance(TokenType.LEFT_PARENTHESES);

        List<ExpressionNode> expressions = new ArrayList<>();
        if (current.type == TokenType.RIGHT_PARENTHESES) {
            Token right = advance();
            return new ArgumentsListNode(expressions, TextRange.combine(left, right));
        }

        if (isPossibleArgumentExpression()) {
            expressions.add(parseArgumentExpression());
        } else {
            addDiagnostic(ParserErrors.ExpressionOrCloseParenthesesExpected, current, current.getRawValue(code));
            Token right = createMissingToken(TokenType.RIGHT_PARENTHESES);
            return new ArgumentsListNode(expressions, TextRange.combine(left, right));
        }

        while (true) {
            if (current.type == TokenType.RIGHT_PARENTHESES) {
                Token right = advance();
                return new ArgumentsListNode(expressions, TextRange.combine(left, right));
            }

            if (current.type == TokenType.COMMA) {
                advance();
                if (isPossibleArgumentExpression()) {
                    expressions.add(parseArgumentExpression());
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                    Token right = createMissingToken(TokenType.RIGHT_PARENTHESES);
                    return new ArgumentsListNode(expressions, TextRange.combine(left, right));
                }
            } else {
                addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                Token right = createMissingToken(TokenType.RIGHT_PARENTHESES);
                return new ArgumentsListNode(expressions, TextRange.combine(left, right));
            }
        }
    }

    private ExpressionNode parseTermWithoutPostfix(int precedence) {
        ExpressionNode expression = switch (current.type) {
            case IDENTIFIER -> isPossibleLambdaExpression() ? parseLambdaExpression() : new NameExpressionNode((IdentifierToken) advance());
            case FALSE -> new BooleanLiteralExpressionNode(false, advance().getRange());
            case TRUE -> new BooleanLiteralExpressionNode(true, advance().getRange());
            case INTEGER_LITERAL -> new IntegerLiteralExpressionNode((IntegerToken) advance());
            case INTEGER64_LITERAL -> new Integer64LiteralExpressionNode((Integer64Token) advance());
            case FLOAT_LITERAL -> new FloatLiteralExpressionNode((FloatToken) advance());
            case STRING_LITERAL -> new StringLiteralExpressionNode((StringToken) advance());
            case CHAR_LITERAL -> new CharLiteralExpressionNode((CharToken) advance());
            case NEW -> parseNewExpression();
            case LEFT_PARENTHESES -> isPossibleLambdaExpression() ? parseLambdaExpression() : parseParenthesizedExpression();
            case LEFT_SQUARE_BRACKET -> parseCollectionExpression();
            case BOOLEAN, INT, INT32, INT64, LONG, CHAR, FLOAT, STRING -> parseStaticReference();
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

        Locatable last = typeNode;
        ExpressionNode lengthExpression = null;
        if (current.type == TokenType.LEFT_SQUARE_BRACKET) {
            advance();
            if (isPossibleExpression()) {
                lengthExpression = parseExpression();
            } else {
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                lengthExpression = new InvalidExpressionNode(createMissingTokenRange());
            }
            last = advance(TokenType.RIGHT_SQUARE_BRACKET);

            typeNode = new ArrayTypeNode(typeNode, TextRange.combine(typeNode, last));
        }

        List<ExpressionNode> items = null;
        if (typeNode.getNodeType() == NodeType.ARRAY_TYPE && current.type == TokenType.LEFT_CURLY_BRACKET) {
            advance();

            items = new ArrayList<>();
            boolean expectExpression = true;
            while (current.type != TokenType.END_OF_FILE) {
                if (expectExpression) {
                    if (current.type == TokenType.RIGHT_CURLY_BRACKET) {
                        last = advance();
                        break;
                    }
                    if (isPossibleExpression()) {
                        items.add(parseExpression());
                    } else {
                        addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                    }
                    expectExpression = false;
                } else {
                    if (current.type == TokenType.RIGHT_CURLY_BRACKET) {
                        last = advance();
                        break;
                    }
                    if (current.type == TokenType.COMMA) {
                        advance(TokenType.COMMA);
                        expectExpression = true;
                    } else {
                        addDiagnostic(ParserErrors.CommaOrCloseCurlyBracketExpected, current);
                        break;
                    }
                }
            }
        }

        return new NewExpressionNode(typeNode, lengthExpression, items, TextRange.combine(newToken, last));
    }

    private ExpressionNode parseParenthesizedExpression() {
        advance(TokenType.LEFT_PARENTHESES);
        ExpressionNode expression = parseExpression();
        advance(TokenType.RIGHT_PARENTHESES);
        return expression;
    }

    private ExpressionNode parseCollectionExpression() {
        Token leftBracket = advance(TokenType.LEFT_SQUARE_BRACKET);

        List<ExpressionNode> items = new ArrayList<>();
        boolean expectExpression = true;
        while (current.type != TokenType.END_OF_FILE) {
            if (expectExpression) {
                if (current.type == TokenType.RIGHT_SQUARE_BRACKET) {
                    break;
                }
                if (isPossibleExpression()) {
                    items.add(parseExpression());
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                }
                expectExpression = false;
            } else {
                if (current.type == TokenType.RIGHT_SQUARE_BRACKET) {
                    break;
                }
                if (current.type == TokenType.COMMA) {
                    advance(TokenType.COMMA);
                    expectExpression = true;
                } else {
                    addDiagnostic(ParserErrors.CommaOrCloseSquareBracketExpected, current);
                    break;
                }
            }
        }

        Token rightBracket = advance(TokenType.RIGHT_SQUARE_BRACKET);

        return new CollectionExpressionNode(items, TextRange.combine(leftBracket, rightBracket));
    };

    private LambdaExpressionNode parseLambdaExpression() {
        Token first = current;
        List<NameExpressionNode> parameters = new ArrayList<>();
        if (current.type == TokenType.IDENTIFIER) {
            parameters.add(new NameExpressionNode((IdentifierToken) advance()));
            advance(TokenType.EQUAL_GREATER);
        } else if (current.type == TokenType.LEFT_PARENTHESES) {
            advance();
            if (current.type == TokenType.RIGHT_PARENTHESES) {
                advance();
            } else {
                while (true) {
                    if (current.type == TokenType.IDENTIFIER) {
                        parameters.add(new NameExpressionNode((IdentifierToken) advance()));
                        if (current.type == TokenType.RIGHT_PARENTHESES) {
                            advance();
                            break;
                        } else if (current.type == TokenType.COMMA) {
                            advance();
                        } else {
                            addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                            break;
                        }
                    }
                }
            }
            advance(TokenType.EQUAL_GREATER);
        } else {
            throw new InternalException("Check isPossibleLambdaExpression() method.");
        }

        StatementNode statement;
        if (current.type == TokenType.LEFT_CURLY_BRACKET) {
            statement = parseBlockStatement();
        } else {
            if (isPossibleSimpleStatement() && !isPossibleDeclaration()) {
                statement = parseSimpleStatementNotDeclaration();
            } else {
                statement = new InvalidStatementNode(createMissingTokenRange());
                addDiagnostic(ParserErrors.SimpleStatementExpected, current, current.getRawValue(code));
            }
        }

        return new LambdaExpressionNode(parameters, statement, TextRange.combine(first, statement));
    }

    private StaticReferenceNode parseStaticReference() {
        Token token = advance();
        return new StaticReferenceNode(switch (token.type) {
            case BOOLEAN -> PredefinedType.BOOLEAN;
            case INT, INT32 -> PredefinedType.INT;
            case INT64, LONG -> PredefinedType.INT64;
            case CHAR -> PredefinedType.CHAR;
            case FLOAT -> PredefinedType.FLOAT;
            case STRING -> PredefinedType.STRING;
            default -> throw new InternalException();
        }, token.getRange());
    }

    private boolean isPossibleExpression() {
        switch (current.type) {
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
            case INT:
            case INT32:
            case INT64:
            case LONG:
            case FLOAT:
            case STRING:
            case CHAR:
            case PLUS:
            case MINUS:
            case EXCLAMATION:
            case AWAIT:
                return true;

            default:
                return false;
        }
    }

    private boolean isPossibleArgumentExpression() {
        if (current.type == TokenType.REF) {
            return true;
        } else {
            return isPossibleExpression();
        }
    }

    private boolean isPossibleLambdaExpression() {
        // x => ...
        if (current.type == TokenType.IDENTIFIER) {
            return peek(1).type == TokenType.EQUAL_GREATER;
        }

        if (current.type == TokenType.LEFT_PARENTHESES) {
            // () => ...
            if (peek(1).type == TokenType.RIGHT_PARENTHESES && peek(2).type == TokenType.EQUAL_GREATER) {
                return true;
            }

            // (x) => ...
            // (x, y, z) => ...
            if (peek(1).type == TokenType.IDENTIFIER) {
                boolean commaExpected = true;
                int index = 2;
                while (true) {
                    Token next = peek(index++);
                    if (commaExpected) {
                        if (next.type == TokenType.COMMA) {
                            commaExpected = false;
                        } else if (next.type == TokenType.RIGHT_PARENTHESES) {
                            return peek(index).type == TokenType.EQUAL_GREATER;
                        } else {
                            return false;
                        }
                    } else {
                        if (next.type == TokenType.IDENTIFIER) {
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
        switch (current.type) {
            case BOOLEAN:
            case INT:
            case INT32:
            case INT64:
            case LONG:
            case FLOAT:
            case STRING:
            case CHAR:
                return true;

            default:
                return false;
        }
    }

    private TypeNode parseRefTypeNode() {
        if (current.type == TokenType.REF) {
            Token ref = advance();
            TypeNode underlying = parseTypeNode();
            return new RefTypeNode(underlying, TextRange.combine(ref, underlying));
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseLetTypeNode() {
        if (current.type == TokenType.LET) {
            Token token = advance();
            return new LetTypeNode(token.getRange());
        } else {
            return parseTypeNode();
        }
    }

    private TypeNode parseTypeNode() {
        TypeNode type = switch (current.type) {
            case BOOLEAN -> new PredefinedTypeNode(PredefinedType.BOOLEAN, current.getRange());
            case INT, INT32 -> new PredefinedTypeNode(PredefinedType.INT, current.getRange());
            case INT64, LONG -> new PredefinedTypeNode(PredefinedType.INT64, current.getRange());
            case FLOAT -> new PredefinedTypeNode(PredefinedType.FLOAT, current.getRange());
            case STRING -> new PredefinedTypeNode(PredefinedType.STRING, current.getRange());
            case CHAR -> new PredefinedTypeNode(PredefinedType.CHAR, current.getRange());
            case IDENTIFIER -> new CustomTypeNode(((IdentifierToken) current).value, current.getRange());
            default -> new InvalidTypeNode(current.getRange());
        };

        advance();

        while (true) {
            if (current.type == TokenType.LEFT_SQUARE_BRACKET && peek(1).type == TokenType.RIGHT_SQUARE_BRACKET) {
                advance();
                Token right = advance();
                type = new ArrayTypeNode(type, TextRange.combine(type, right));
            } else {
                break;
            }
        }

        return type;
    }

    private Token advance() {
        last = current;

        Token match = current;
        current = tokens.next();
        while (isSkippable(current.type)) {
            current = tokens.next();
        }
        return match;
    }

    private Token advance(TokenType type) {
        if (current.type == type) {
            last = current;

            Token match = current;
            current = tokens.next();
            while (isSkippable(current.type)) {
                current = tokens.next();
            }
            return match;
        } else {
            switch (type) {
                case LEFT_PARENTHESES -> addDiagnostic(ParserErrors.LeftParenthesisExpected, current, current.getRawValue(code));
                case RIGHT_PARENTHESES -> addDiagnostic(ParserErrors.RightParenthesisExpected, current, current.getRawValue(code));
                case LEFT_CURLY_BRACKET -> addDiagnostic(ParserErrors.OpenCurlyBracketExpected, current, current.getRawValue(code));
                case RIGHT_CURLY_BRACKET -> addDiagnostic(ParserErrors.CloseCurlyBracketExpected, current, current.getRawValue(code));
                case RIGHT_SQUARE_BRACKET -> addDiagnostic(ParserErrors.CloseSquareBracketExpected, current, current.getRawValue(code));
                case SEMICOLON -> addDiagnostic(ParserErrors.SemicolonExpected, last);
                case COLON -> addDiagnostic(ParserErrors.ColonExpected, last);
                case IN -> addDiagnostic(ParserErrors.InExpected, current);
                default -> throw new RuntimeException("Not implemented");
            }

            return switch (type) {
                case SEMICOLON -> createMissingTokenAfterLast(type);
                default -> createMissingToken(type);
            };
        }
    }

    private Token peek(int n) {
        int shift = 1;
        while (true) {
            if (!isSkippable(tokens.peek(shift).type)) {
                n--;
                if (n == 0) {
                    break;
                }
            }
            shift++;
        }
        return tokens.peek(shift);
    }

    private boolean isSkippable(TokenType type) {
        return type == TokenType.WHITESPACE || type == TokenType.LINE_BREAK || type == TokenType.COMMENT;
    }

    private TextRange nextCharacter(TextRange range) {
        return new SingleLineTextRange(range.getLine2(), range.getColumn2(), range.getPosition() + range.getLength() + 1, 1);
    }

    private void addDiagnostic(ErrorCode code, TextRange range) {
        diagnostics.add(new DiagnosticMessage(code, range));
    }

    private void addDiagnostic(ErrorCode code, Locatable locatable, Object... parameters) {
        locatable = handleEndOfFile(locatable);
        diagnostics.add(new DiagnosticMessage(code, locatable, parameters));
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
        Locatable locatable = handleEndOfFile(current);
        return new SingleLineTextRange(
                locatable.getRange().getLine1(),
                locatable.getRange().getColumn1(),
                locatable.getRange().getPosition(),
                0);
    }

    private Locatable handleEndOfFile(Locatable locatable) {
        if (locatable == EndOfFileToken.instance) {
            Token last = tokens.last();
            return () -> new SingleLineTextRange(
                    last.getRange().getLine2(),
                    last.getRange().getColumn2(),
                    last.getRange().getPosition() + last.getRange().getLength(),
                    1);
        } else {
            return locatable;
        }
    }
}