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
        List<StatementNode> statements = new ArrayList<>();
        while (current.type != TokenType.END_OF_FILE) {
            if (isPossibleStatement(current.type)) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            }
        }

        TextRange range = statements.isEmpty() ?
                new SingleLineTextRange(1, 1, 0, 0) :
                TextRange.combine(statements.get(0), statements.get(statements.size() - 1));
        return new CompilationUnitNode(statements, range);
    }

    private BlockStatementNode parseBlockStatement() {
        Token first = advance(TokenType.LEFT_CURLY_BRACKET);

        List<StatementNode> statements = new ArrayList<>();
        while (current.type != TokenType.RIGHT_CURLY_BRACKET && current.type != TokenType.END_OF_FILE) {
            if (isPossibleStatement(current.type)) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            }
        }

        Token last = advance(TokenType.RIGHT_CURLY_BRACKET);
        return new BlockStatementNode(statements, TextRange.combine(first, last));
    }

    private boolean isPossibleStatement(TokenType type) {
        switch (type) {
            case LEFT_CURLY_BRACKET:
            case SEMICOLON:
            case IDENTIFIER:
            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
                return true;

            default:
                return false;
        }
    }

    private StatementNode parseStatement() {
        switch (current.type) {
            case LEFT_CURLY_BRACKET -> {
                return parseBlockStatement();
            }

            case SEMICOLON -> {
                Token semicolon = advance();
                return new EmptyStatementNode(semicolon.getRange());
            }

            case BOOLEAN, INT, FLOAT, STRING -> {
                TypeNode type = parseTypeNode();
                if (current.type == TokenType.IDENTIFIER) {
                    IdentifierToken identifier = (IdentifierToken) advance();
                    NameExpressionNode name = new NameExpressionNode(identifier);
                    switch (current.type) {
                        case SEMICOLON -> {
                            Token semicolon = advance();
                            return new VariableDeclarationNode(type, name, TextRange.combine(type, semicolon));
                        }
                        case EQUAL -> {
                            advance();
                            if (isPossibleExpression()) {
                                ExpressionNode expression = parseExpression();
                                Token semicolon = advance(TokenType.SEMICOLON);
                                return new VariableDeclarationNode(type, name, expression, TextRange.combine(type, semicolon));
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

            case IDENTIFIER -> {
                // TODO: custom types
                ExpressionNode expression1 = parseExpression();
                AssignmentOperator assignment = switch (current.type) {
                    case EQUAL -> AssignmentOperator.ASSIGNMENT;
                    case PLUS_EQUAL -> AssignmentOperator.PLUS_ASSIGNMENT;
                    case MINUS_EQUAL -> AssignmentOperator.MINUS_ASSIGNMENT;
                    case ASTERISK_EQUAL -> AssignmentOperator.MULTIPLY_ASSIGNMENT;
                    case SLASH_EQUAL -> AssignmentOperator.DIVIDE_ASSIGNMENT;
                    default -> null;
                };

                if (assignment == null) {
                    Token semicolon = advance(TokenType.SEMICOLON);
                    return new ExpressionStatementNode(expression1, TextRange.combine(expression1, semicolon));
                } else {
                    Token assignmentToken = advance();

                    ExpressionNode expression2;
                    Locatable last;
                    if (isPossibleExpression()) {
                        expression2 = parseExpression();
                        last = advance(TokenType.SEMICOLON);
                    } else {
                        addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                        expression2 = new InvalidExpressionNode(createMissingTokenRange());
                        last = expression2;
                    }
                    return new AssignmentStatementNode(
                            expression1,
                            new AssignmentOperatorNode(assignment, assignmentToken.getRange()),
                            expression2,
                            TextRange.combine(expression1, last));
                }
            }
            default -> throw new RuntimeException("TODO");
        }
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
            if (expression instanceof IntegerLiteralExpressionNode integer) {
                left = new IntegerLiteralExpressionNode("-" + integer.value, TextRange.combine(unaryToken, integer));
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
                default -> null;
            };
            if (binary == null) {
                break;
            }

            Token binaryToken = advance();

            int newPrecedence = Precedences.get(binary);
            if (newPrecedence < precedence) {
                break;
            }

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
                        advance();
                        expression = new MemberAccessExpressionNode(expression, identifier.value, TextRange.combine(expression, identifier));
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
            case IDENTIFIER -> new NameExpressionNode((IdentifierToken) current);
            case FALSE -> new BooleanLiteralExpressionNode(false, current.getRange());
            case TRUE -> new BooleanLiteralExpressionNode(true, current.getRange());
            case INTEGER_LITERAL -> new IntegerLiteralExpressionNode(((IntegerToken) current).value, current.getRange());

            // () => {}
            // new ...

            default -> null;
        };

        if (expression != null) {
            advance();
            return expression;
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            return new InvalidExpressionNode(createMissingTokenRange());
        }
    }

    private boolean isPossibleExpression() {
        switch (current.type) {
            case FALSE:
            case TRUE:
            case LEFT_PARENTHESES:
            case INTEGER_LITERAL:
            case NEW:
            case IDENTIFIER:
            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
            case PLUS:
            case MINUS:
            case EXCLAMATION:
                return true;

            default:
                return false;
        }
    }

    private boolean isPossibleVariableDeclaration() {
        switch (current.type) {
            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
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
            default -> throw new RuntimeException("Unknown type.");
        };

        advance();

        while (true) {
            if (current.type == TokenType.LEFT_SQUARE_BRACKET) {
                advance();
                Token right;
                if (current.type == TokenType.RIGHT_SQUARE_BRACKET) {
                    right = advance();
                } else {
                    addDiagnostic(ParserErrors.CloseSquareBracketExpected, current, current.getRawValue(code));
                    right = createMissingToken(TokenType.RIGHT_SQUARE_BRACKET);
                }
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
                default -> throw new RuntimeException("Not implemented");
            }

            return createMissingToken(type);
        }
    }

    private void addDiagnostic(ErrorCode code, Token token, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, token, parameters));
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