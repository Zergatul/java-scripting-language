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
        final int parseStaticVariables = 1;
        final int parseFunctions = 2;
        final int parseStatements = 3;

        List<VariableDeclarationNode> variables = new ArrayList<>();
        List<FunctionNode> functions = new ArrayList<>();
        List<StatementNode> statements = new ArrayList<>();

        int state = parseStaticVariables;
        while (current.type != TokenType.END_OF_FILE) {
            switch (state) {
                case parseStaticVariables -> {
                    if (current.type == TokenType.STATIC) {
                        advance();
                        StatementNode statement = parseVariableDeclaration();
                        if (statement instanceof VariableDeclarationNode declaration) {
                            variables.add(declaration);
                            advance(TokenType.SEMICOLON);
                        }
                    } else {
                        state = parseFunctions;
                    }
                }
                case parseFunctions -> {
                    if (isPossibleFunction()) {
                        functions.add(parseFunction());
                    } else {
                        state = parseStatements;
                    }
                }
                case parseStatements -> {
                    if (isPossibleStatement()) {
                        statements.add(parseStatement());
                    } else {
                        addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
                        advance();
                    }
                }
            }
        }

        Node first;
        if (!variables.isEmpty()) {
            first = variables.get(0);
        } else if (!functions.isEmpty()) {
            first = functions.get(0);
        } else if (!statements.isEmpty()) {
            first = statements.get(0);
        } else {
            first = null;
        }

        Node last;
        if (!statements.isEmpty()) {
            last = statements.get(statements.size() - 1);
        } else if (!functions.isEmpty()) {
            last = functions.get(functions.size() - 1);
        } else if (!variables.isEmpty()) {
            last = variables.get(variables.size() - 1);
        } else {
            last = null;
        }

        TextRange range = first == null ?
                new SingleLineTextRange(1, 1, 0, 0) :
                TextRange.combine(first, last);
        return new CompilationUnitNode(variables, functions, statements, range);
    }

    private BlockStatementNode parseBlockStatement() {
        Token first = advance(TokenType.LEFT_CURLY_BRACKET);

        List<StatementNode> statements = new ArrayList<>();
        while (current.type != TokenType.RIGHT_CURLY_BRACKET && current.type != TokenType.END_OF_FILE) {
            if (isPossibleStatement()) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            }
        }

        Token last = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new BlockStatementNode(statements, TextRange.combine(first, last));
    }

    private IfStatementNode parseIfStatement() {
        Token ifToken = advance(TokenType.IF);

        advance(TokenType.LEFT_PARENTHESES);

        ExpressionNode condition;
        if (isPossibleExpression()) {
            condition = parseExpression();
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            condition = new InvalidExpressionNode(createMissingTokenRange());
        }

        advance(TokenType.RIGHT_PARENTHESES);

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

        return new IfStatementNode(condition, thenStatement, elseStatement, TextRange.combine(ifToken, elseStatement == null ? thenStatement : elseStatement));
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
        StatementNode body = parseStatement();

        return new ForLoopStatementNode(init, condition, update, body, TextRange.combine(forToken, body));
    }

    private ForEachLoopStatementNode parseForEachLoopStatement() {
        Token foreachToken = advance(TokenType.FOREACH);
        advance(TokenType.LEFT_PARENTHESES);

        TypeNode typeNode;
        if (isPossibleDeclaration()) {
            typeNode = parseTypeNode();
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
        throw new InternalException(); // TODO
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

    private FunctionNode parseFunction() {
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

        return new FunctionNode(returnType, name, parameters, body, TextRange.combine(returnType, body));
    }

    private ParameterListNode parseParameterList() {
        Token begin = advance(TokenType.LEFT_PARENTHESES);
        Token end;

        List<ParameterNode> parameters = new ArrayList<>();

        if (current.type == TokenType.RIGHT_PARENTHESES) {
            end = advance();
        } else {
            while (true) {
                TypeNode type = parseTypeNode();
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

    private StatementNode parseVariableDeclaration() {
        TypeNode type = parseTypeNode();
        if (current.type == TokenType.IDENTIFIER) {
            IdentifierToken identifier = (IdentifierToken) advance();
            NameExpressionNode name = new NameExpressionNode(identifier);
            switch (current.type) {
                case SEMICOLON -> {
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
                    return new InvalidStatementNode(createMissingTokenRange());
                }
            }
        } else {
            addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
            return new InvalidStatementNode(createMissingTokenRange());
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
            Locatable last;
            if (isPossibleExpression()) {
                expression2 = parseExpression();
            } else {
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                expression2 = new InvalidExpressionNode(createMissingTokenRange());
                last = expression2;
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

            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
            case CHAR:
            case IDENTIFIER:
            case LEFT_PARENTHESES:
                return true;

            default:
                return false;
        }
    }

    private boolean isPossibleSimpleStatement() {
        switch (current.type) {
            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
            case CHAR:
            case IDENTIFIER:
            case LEFT_PARENTHESES:
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
            case BOOLEAN, INT, FLOAT, STRING, CHAR, IDENTIFIER, LEFT_PARENTHESES -> parseSimpleStatement().expand(advance(TokenType.SEMICOLON));
            default -> {
                if (isPossibleExpression()) {
                    yield parseSimpleStatement().expand(advance(TokenType.SEMICOLON));
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
            if (newPrecedence < precedence) {
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
                    advance();
                    if (current.type == TokenType.IDENTIFIER) {
                        IdentifierToken identifier = (IdentifierToken) current;
                        NameExpressionNode name = new NameExpressionNode(identifier.value, identifier.getRange());
                        advance();
                        expression = new MemberAccessExpressionNode(expression, name, TextRange.combine(expression, name));
                    } else {
                        addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                        return expression;
                    }
                }

                default -> {
                    return expression;
                }
            }
        }
    }

    private ArgumentsListNode parseArgumentsList() {
        Token left = advance(TokenType.LEFT_PARENTHESES);

        List<ExpressionNode> expressions = new ArrayList<>();
        if (current.type == TokenType.RIGHT_PARENTHESES) {
            Token right = advance();
            return new ArgumentsListNode(expressions, TextRange.combine(left, right));
        }

        if (isPossibleExpression()) {
            expressions.add(parseExpression());
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
                if (isPossibleExpression()) {
                    expressions.add(parseExpression());
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
            case FLOAT_LITERAL -> new FloatLiteralExpressionNode((FloatToken) advance());
            case STRING_LITERAL -> new StringLiteralExpressionNode((StringToken) advance());
            case CHAR_LITERAL -> new CharLiteralExpressionNode((CharToken) advance());
            case NEW -> parseNewExpression();
            case LEFT_PARENTHESES -> isPossibleLambdaExpression() ? parseLambdaExpression() : parseParenthesizedExpression();
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
                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRange());
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
                        addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRange());
                    }
                    expectExpression = false;
                } else {
                    if (current.type == TokenType.RIGHT_CURLY_BRACKET) {
                        last = advance();
                        break;
                    }
                    advance(TokenType.COMMA);
                    expectExpression = true;
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

    private boolean isPossibleExpression() {
        switch (current.type) {
            case FALSE:
            case TRUE:
            case LEFT_PARENTHESES:
            case INTEGER_LITERAL:
            case FLOAT_LITERAL:
            case STRING_LITERAL:
            case CHAR_LITERAL:
            case NEW:
            case IDENTIFIER:
            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
            case CHAR:
            case PLUS:
            case MINUS:
            case EXCLAMATION:
                return true;

            default:
                return false;
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
            case FLOAT:
            case STRING:
            case CHAR:
                return true;

            default:
                return false;
        }
    }

    private TypeNode parseTypeNode() {
        TypeNode type = switch (current.type) {
            case BOOLEAN -> new PredefinedTypeNode(PredefinedType.BOOLEAN, current.getRange());
            case INT -> new PredefinedTypeNode(PredefinedType.INT, current.getRange());
            case FLOAT -> new PredefinedTypeNode(PredefinedType.FLOAT, current.getRange());
            case STRING -> new PredefinedTypeNode(PredefinedType.STRING, current.getRange());
            case CHAR -> new PredefinedTypeNode(PredefinedType.CHAR, current.getRange());
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
        Token match = current;
        current = tokens.next();
        while (current.type == TokenType.WHITESPACE) {
            current = tokens.next();
        }
        return match;
    }

    private Token advance(TokenType type) {
        if (current.type == type) {
            Token match = current;
            current = tokens.next();
            while (current.type == TokenType.WHITESPACE) {
                current = tokens.next();
            }
            return match;
        } else {
            switch (type) {
                case LEFT_CURLY_BRACKET -> addDiagnostic(ParserErrors.OpenCurlyBracketExpected, current, current.getRawValue(code));
                case RIGHT_SQUARE_BRACKET -> addDiagnostic(ParserErrors.CloseSquareBracketExpected, current, current.getRawValue(code));
                case SEMICOLON -> addDiagnostic(ParserErrors.SemicolonExpected, current);
                case IN -> addDiagnostic(ParserErrors.InExpected, current);
                default -> throw new RuntimeException("Not implemented");
            }

            return createMissingToken(type);
        }
    }

    private Token peek(int n) {
        int shift = 1;
        while (true) {
            if (tokens.peek(shift).type != TokenType.WHITESPACE) {
                n--;
                if (n == 0) {
                    break;
                }
            }
            shift++;
        }
        return tokens.peek(shift);
    }

    private void addDiagnostic(ErrorCode code, Locatable locatable, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, locatable, parameters));
    }

    private Token createMissingToken(TokenType type) {
        return new Token(type, createMissingTokenRange());
    }

    private TextRange createMissingTokenRange() {
        return new SingleLineTextRange(
                current.getRange().getLine1(),
                current.getRange().getColumn1(),
                current.getRange().getPosition(),
                0);
    }
}