package com.zergatul.scripting.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.ErrorCode;
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
        return new ParserOutput(parseCompilationUnit(), diagnostics);
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
        return new CompilationUnitNode(statements);
    }

    private BlockStatementNode parseBlockStatement() {
        advance(TokenType.LEFT_CURLY_BRACKET);

        List<StatementNode> statements = new ArrayList<>();
        while (current.type != TokenType.RIGHT_CURLY_BRACKET && current.type != TokenType.END_OF_FILE) {
            if (isPossibleStatement(current.type)) {
                statements.add(parseStatement());
            } else {
                addDiagnostic(ParserErrors.StatementExpected, current, current.getRawValue(code));
            }
        }

        advance(TokenType.RIGHT_CURLY_BRACKET);
        return new BlockStatementNode(statements);
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
            case LEFT_CURLY_BRACKET:
                return parseBlockStatement();

            case SEMICOLON:
                advance();
                return new EmptyStatementNode();

            case BOOLEAN:
            case INT:
            case FLOAT:
            case STRING:
                TypeNode type = parseTypeNode();
                if (current.type == TokenType.IDENTIFIER) {
                    IdentifierToken identifier = (IdentifierToken) current;
                    advance();
                    switch (current.type) {
                        case SEMICOLON:
                            advance();
                            return new VariableDeclarationNode(type, identifier.value);

                        case EQUAL:
                            advance();
                            if (isPossibleExpression()) {
                                ExpressionNode expression = parseExpression();
                                advance(TokenType.SEMICOLON);
                                return new VariableDeclarationNode(type, identifier.value, expression);
                            } else {
                                addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                                return new VariableDeclarationNode(type, identifier.value, new InvalidExpressionNode());
                            }

                        default:
                            addDiagnostic(ParserErrors.SemicolonOrEqualExpected, current, current.getRawValue(code));
                            return new InvalidStatementNode();
                    }
                } else {
                    addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                    return new InvalidStatementNode();
                }

            case IDENTIFIER:
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
                    advance(TokenType.SEMICOLON);
                    return new ExpressionStatementNode(expression1);
                } else {
                    advance();

                    ExpressionNode expression2;
                    if (isPossibleExpression()) {
                        expression2 = parseExpression();
                        advance(TokenType.SEMICOLON);
                    } else {
                        addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                        expression2 = new InvalidExpressionNode();
                    }
                    return new AssignmentStatementNode(expression1, assignment, expression2);
                }

            default:
                throw new RuntimeException("TODO");
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
            advance();
            ExpressionNode expression = parseExpressionCore(Precedences.get(unary));
            if (expression instanceof IntegerLiteralExpressionNode integer) {
                left = new IntegerLiteralExpressionNode("-" + integer.value);
            } else {
                left = new UnaryExpressionNode(unary, expression);
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

            advance();

            int newPrecedence = Precedences.get(binary);
            if (newPrecedence < precedence) {
                break;
            }

            if (isPossibleExpression()) {
                left = new BinaryExpressionNode(left, binary, parseExpressionCore(newPrecedence));
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
            return new ConditionalExpressionNode(left, whenTrue, whenFalse);
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
                case LEFT_PARENTHESES:
                    List<ExpressionNode> arguments = parseParenthesizedArgumentList();
                    expression = new InvocationExpressionNode(expression, arguments);
                    break;

                case LEFT_SQUARE_BRACKET:
                    expression = new IndexExpressionNode(expression, parseIndexExpression());
                    break;

                case DOT:
                    advance();
                    if (current.type == TokenType.IDENTIFIER) {
                        String identifier = ((IdentifierToken) current).value;
                        advance();
                        expression = new MemberAccessExpressionNode(expression, identifier);
                    } else {
                        addDiagnostic(ParserErrors.IdentifierExpected, current, current.getRawValue(code));
                        return expression;
                    }
                    break;

                default:
                    return expression;
            }
        }
    }

    private List<ExpressionNode> parseParenthesizedArgumentList() {
        advance(TokenType.LEFT_PARENTHESES);

        List<ExpressionNode> expressions = new ArrayList<>();
        if (current.type == TokenType.RIGHT_PARENTHESES) {
            advance();
            return expressions;
        }

        if (isPossibleExpression()) {
            expressions.add(parseExpression());
        } else {
            addDiagnostic(ParserErrors.ExpressionOrCloseParenthesesExpected, current, current.getRawValue(code));
            return expressions;
        }

        while (true) {
            if (current.type == TokenType.RIGHT_PARENTHESES) {
                advance();
                return expressions;
            }

            if (current.type == TokenType.COMMA) {
                advance();
                if (isPossibleExpression()) {
                    expressions.add(parseExpression());
                } else {
                    addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
                    return expressions;
                }
            } else {
                addDiagnostic(ParserErrors.CommaOrCloseParenthesesExpected, current, current.getRawValue(code));
                return expressions;
            }
        }
    }

    private ExpressionNode parseIndexExpression() {
        advance(TokenType.LEFT_SQUARE_BRACKET);

        if (isPossibleExpression()) {
            ExpressionNode expression = parseExpression();
            advance(TokenType.RIGHT_SQUARE_BRACKET);
            return expression;
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            return new InvalidExpressionNode();
        }
    }

    private ExpressionNode parseTermWithoutPostfix(int precedence) {
        ExpressionNode expression = switch (current.type) {
            case IDENTIFIER -> new NameExpressionNode(((IdentifierToken) current).value);
            case FALSE -> new BooleanLiteralExpressionNode(false);
            case TRUE -> new BooleanLiteralExpressionNode(true);
            case INTEGER_LITERAL -> new IntegerLiteralExpressionNode(((IntegerToken) current).value);

            // () => {}
            // new ...

            default -> null;
        };

        if (expression != null) {
            advance();
            return expression;
        } else {
            addDiagnostic(ParserErrors.ExpressionExpected, current, current.getRawValue(code));
            return new InvalidExpressionNode();
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
            case BOOLEAN -> new PredefinedTypeNode(PredefinedType.BOOLEAN);
            case INT -> new PredefinedTypeNode(PredefinedType.INT);
            case FLOAT -> new PredefinedTypeNode(PredefinedType.FLOAT);
            case STRING -> new PredefinedTypeNode(PredefinedType.STRING);
            default -> throw new RuntimeException("Unknown type.");
        };

        advance();

        while (true) {
            if (current.type == TokenType.LEFT_SQUARE_BRACKET) {
                advance();
                if (current.type == TokenType.RIGHT_SQUARE_BRACKET) {
                    advance();
                } else {
                    addDiagnostic(ParserErrors.CloseSquareBracketExpected, current, current.getRawValue(code));
                }
                type = new ArrayTypeNode(type);
            } else {
                break;
            }
        }

        return type;
    }

    private void advance() {
        current = tokens.next();
        while (current.type == TokenType.WHITESPACE) {
            current = tokens.next();
        }
    }

    private void advance(TokenType type) {
        if (current.type == type) {
            current = tokens.next();
            while (current.type == TokenType.WHITESPACE) {
                current = tokens.next();
            }
            // return used token?
        } else {
            // create missing and return?
            switch (type) {
                case LEFT_CURLY_BRACKET -> addDiagnostic(ParserErrors.OpenCurlyBracketExpected, current, current.getRawValue(code));
                case RIGHT_SQUARE_BRACKET -> addDiagnostic(ParserErrors.CloseSquareBracketExpected, current, current.getRawValue(code));
                case SEMICOLON -> addDiagnostic(ParserErrors.SemicolonExpected, current);
                default -> throw new RuntimeException("Not implemented");
            }
        }
    }

    private void addDiagnostic(ErrorCode code, Token token, Object... parameters) {
        diagnostics.add(new DiagnosticMessage(code, token, parameters));
    }
}