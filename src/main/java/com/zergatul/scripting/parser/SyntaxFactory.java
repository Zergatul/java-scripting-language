package com.zergatul.scripting.parser;

import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.*;

import java.util.List;

public final class SyntaxFactory {

    private static final Lazy<MissingNodes> NODES = new Lazy<>(MissingNodes::new);

    public static ArgumentsListNode missingArgumentList() {
        return NODES.value().argumentsList;
    }

    public static AssignmentOperatorNode missingAssignmentOperator() {
        return NODES.value().assignmentOperator;
    }

    public static AssignmentStatementNode missingAssignmentStatement() {
        return NODES.value().assignmentStatement;
    }

    public static BinaryExpressionNode missingBinaryExpression() {
        return NODES.value().binaryExpression;
    }

    public static BinaryOperatorNode missingBinaryOperator() {
        return NODES.value().binaryOperator;
    }

    public static BlockStatementNode missingBlockStatement() {
        return NODES.value().blockStatement;
    }

    public static ConstructorInitializerNode missingConstructorInitializer() {
        return NODES.value().constructorInitializer;
    }

    public static ExpressionNode missingExpression() {
        return NODES.value().expression;
    }

    public static ExpressionStatementNode missingExpressionStatement() {
        return NODES.value().expressionStatement;
    }

    public static IfStatementNode missingIfStatement() {
        return NODES.value().ifStatement;
    }

    public static IndexExpressionNode missingIndexExpression() {
        return NODES.value().indexExpression;
    }

    public static IntegerLiteralExpressionNode missingIntegerLiteralExpression() {
        return NODES.value().integerLiteralExpression;
    }

    public static InvocationExpressionNode missingInvocationExpression() {
        return NODES.value().invocationExpression;
    }

    public static LambdaExpressionNode missingLambdaExpression() {
        return NODES.value().lambdaExpression;
    }

    public static NameExpressionNode missingNameExpression() {
        return NODES.value().nameExpression;
    }

    public static PostfixStatementNode missingPostfixStatement() {
        return NODES.value().postfixStatement;
    }

    public static MemberAccessExpressionNode missingMemberAccessExpression() {
        return NODES.value().memberAccessExpression;
    }

    public static ReturnStatementNode missingReturnStatement() {
        return NODES.value().returnStatement;
    }

    public static StatementsListNode missingStatementsList() {
        return NODES.value().statementsList;
    }

    public static TypeNode missingType() {
        return NODES.value().type;
    }

    public static UnaryExpressionNode missingUnaryExpression() {
        return NODES.value().unaryExpression;
    }

    public static UnaryOperatorNode missingUnaryOperator() {
        return NODES.value().unaryOperator;
    }

    public static VariableDeclarationNode missingVariableDeclaration() {
        return NODES.value().variableDeclaration;
    }

    public static WhileLoopStatementNode missingWhileLoopStatement() {
        return NODES.value().whileLoopStatement;
    }

    private static final class MissingNodes {

        public final ArgumentsListNode argumentsList;
        public final AssignmentOperatorNode assignmentOperator;
        public final BinaryOperatorNode binaryOperator;
        public final BlockStatementNode blockStatement;
        public final ExpressionNode expression;
        public final IntegerLiteralExpressionNode integerLiteralExpression;
        public final LetTypeNode letType;
        public final NameExpressionNode nameExpression;
        public final ReturnStatementNode returnStatement;
        public final StatementsListNode statementsList;
        public final TypeNode type;
        public final UnaryOperatorNode unaryOperator;
        public final VariableDeclarationNode variableDeclaration;

        public final AssignmentStatementNode assignmentStatement;
        public final BinaryExpressionNode binaryExpression;
        public final ConstructorInitializerNode constructorInitializer;
        public final ExpressionStatementNode expressionStatement;
        public final IfStatementNode ifStatement;
        public final IndexExpressionNode indexExpression;
        public final InvocationExpressionNode invocationExpression;
        public final LambdaExpressionNode lambdaExpression;
        public final PostfixStatementNode postfixStatement;
        public final MemberAccessExpressionNode memberAccessExpression;
        public final UnaryExpressionNode unaryExpression;
        public final WhileLoopStatementNode whileLoopStatement;

        public MissingNodes() {
            argumentsList = new ArgumentsListNode(Token.MISSING, SeparatedList.of(), Token.MISSING);
            assignmentOperator = new AssignmentOperatorNode(Token.MISSING, AssignmentOperator.ASSIGNMENT, TextRange.MISSING);
            binaryOperator = new BinaryOperatorNode(Token.MISSING, BinaryOperator.PLUS);
            blockStatement = new BlockStatementNode(Token.MISSING, List.of(), Token.MISSING);
            expression = new NameExpressionNode(ValueToken.MISSING);
            integerLiteralExpression = new IntegerLiteralExpressionNode(null, ValueToken.MISSING);
            letType = new LetTypeNode(Token.MISSING);
            nameExpression = new NameExpressionNode(ValueToken.MISSING);
            returnStatement = new ReturnStatementNode(Token.MISSING, null, Token.MISSING);
            statementsList = new StatementsListNode(List.of(), TextRange.MISSING);
            type = new InvalidTypeNode(Token.MISSING);
            unaryOperator = new UnaryOperatorNode(Token.MISSING, UnaryOperator.PLUS);
            variableDeclaration = new VariableDeclarationNode(letType, nameExpression, null, null, Token.MISSING);

            assignmentStatement = new AssignmentStatementNode(expression, assignmentOperator, expression, null, TextRange.MISSING);
            binaryExpression = new BinaryExpressionNode(expression, binaryOperator, expression);
            constructorInitializer = new ConstructorInitializerNode(Token.MISSING, argumentsList);
            expressionStatement = new ExpressionStatementNode(expression, null);
            ifStatement = new IfStatementNode(Token.MISSING, Token.MISSING, expression, Token.MISSING, blockStatement, null, null, TextRange.MISSING);
            indexExpression = new IndexExpressionNode(expression, Token.MISSING, expression, Token.MISSING);
            invocationExpression = new InvocationExpressionNode(expression, argumentsList, TextRange.MISSING);
            lambdaExpression = new LambdaExpressionNode(Token.MISSING, SeparatedList.of(), Token.MISSING, Token.MISSING, blockStatement);
            postfixStatement = new PostfixStatementNode(ParserNodeType.INCREMENT_STATEMENT, expression, Token.MISSING, null);
            memberAccessExpression = new MemberAccessExpressionNode(expression, Token.MISSING, nameExpression);
            unaryExpression = new UnaryExpressionNode(unaryOperator, expression);
            whileLoopStatement = new WhileLoopStatementNode(Token.MISSING, Token.MISSING, expression, Token.MISSING, blockStatement);
        }
    }
}