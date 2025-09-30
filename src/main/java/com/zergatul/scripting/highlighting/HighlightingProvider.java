package com.zergatul.scripting.highlighting;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.nodes.*;

import java.util.ArrayList;
import java.util.List;

import static com.zergatul.scripting.highlighting.SemanticTokenType.*;

public class HighlightingProvider {

    private final List<Line> lines;
    private final BinderOutput output;
    private final List<SemanticToken> result;

    public HighlightingProvider(LexerOutput lexerOutput, BinderOutput binderOutput) {
        this.lines = lexerOutput.lines();
        this.output = binderOutput;
        this.result = new ArrayList<>();
    }

    public List<SemanticToken> get() {
        process(output.unit());
        return result;
    }

    private void process(BoundNode node) {
        switch (node.getNodeType()) {
            case ARGUMENTS_LIST -> process((BoundArgumentsListNode) node);
            case ARRAY_CREATION_EXPRESSION -> process((BoundArrayCreationExpressionNode) node);
            case ARRAY_INITIALIZER_EXPRESSION -> process((BoundArrayInitializerExpressionNode) node);
            case ARRAY_TYPE -> process((BoundArrayTypeNode) node);
            case ASSIGNMENT_OPERATOR -> process((BoundAssignmentOperatorNode) node);
            case ASSIGNMENT_STATEMENT -> process((BoundAssignmentStatementNode) node);
            case AUGMENTED_ASSIGNMENT_STATEMENT -> process((BoundAugmentedAssignmentStatementNode) node);
            case AWAIT_EXPRESSION -> process((BoundAwaitExpressionNode) node);
            case BINARY_EXPRESSION -> process((BoundBinaryExpressionNode) node);
            case BINARY_OPERATOR -> process((BoundBinaryOperatorNode) node);
            case BLOCK_STATEMENT -> process((BoundBlockStatementNode) node);
            case BOOLEAN_LITERAL -> process((BoundBooleanLiteralExpressionNode) node);
            case BREAK_STATEMENT -> process((BoundBreakStatementNode) node);
            case CHAR_LITERAL -> process((BoundCharLiteralExpressionNode) node);
            case CLASS_CONSTRUCTOR -> process((BoundClassConstructorNode) node);
            case CLASS_DECLARATION -> process((BoundClassNode) node);
            case CLASS_FIELD -> process((BoundClassFieldNode) node);
            case CLASS_METHOD -> process((BoundClassMethodNode) node);
            case COLLECTION_EXPRESSION -> process((BoundCollectionExpressionNode) node);
            case COMPILATION_UNIT -> process((BoundCompilationUnitNode) node);
            case COMPILATION_UNIT_MEMBERS -> process((BoundCompilationUnitMembersListNode) node);
            case CONDITIONAL_EXPRESSION -> process((BoundConditionalExpressionNode) node);
            case CONTINUE_STATEMENT -> process((BoundContinueStatementNode) node);
            case CONVERSION -> process((BoundConversionNode) node);
            case CUSTOM_TYPE -> process((BoundCustomTypeNode) node);
            case DECREMENT_STATEMENT -> process((BoundPostfixStatementNode) node);
            case EMPTY_COLLECTION_EXPRESSION -> process((BoundEmptyCollectionExpressionNode) node);
            case EMPTY_STATEMENT -> process((BoundEmptyStatementNode) node);
            case EXPRESSION_STATEMENT -> process((BoundExpressionStatementNode) node);
            case FLOAT_LITERAL -> process((BoundFloatLiteralExpressionNode) node);
            case FOREACH_LOOP_STATEMENT -> process((BoundForEachLoopStatementNode) node);
            case FOR_LOOP_STATEMENT -> process((BoundForLoopStatementNode) node);
            case FUNCTION -> process((BoundFunctionNode) node);
            case FUNCTION_AS_LAMBDA -> process((BoundFunctionAsLambdaExpressionNode) node);
            case FUNCTION_INVOCATION -> process((BoundFunctionInvocationExpression) node);
            case FUNCTION_REFERENCE -> process((BoundFunctionReferenceNode) node);
            case FUNCTION_TYPE -> process((BoundFunctionTypeNode) node);
            case GENERATOR_CONTINUE -> process((BoundGeneratorContinueNode) node);
            case GENERATOR_GET_VALUE -> process((BoundGeneratorGetValueNode) node);
            case GENERATOR_RETURN -> process((BoundGeneratorReturnNode) node);
            case IF_STATEMENT -> process((BoundIfStatementNode) node);
            case IMPLICIT_CAST -> process((BoundImplicitCastExpressionNode) node);
            case INCREMENT_STATEMENT -> process((BoundPostfixStatementNode) node);
            case INDEX_EXPRESSION -> process((BoundIndexExpressionNode) node);
            case INTEGER64_LITERAL -> process((BoundInteger64LiteralExpressionNode) node);
            case INTEGER_LITERAL -> process((BoundIntegerLiteralExpressionNode) node);
            case INVALID_EXPRESSION -> process((BoundInvalidExpressionNode) node);
            case INVALID_STATEMENT -> process((BoundInvalidStatementNode) node);
            case INVALID_TYPE -> process((BoundInvalidTypeNode) node);
            case JAVA_TYPE -> process((BoundJavaTypeNode) node);
            case LAMBDA_EXPRESSION -> process((BoundLambdaExpressionNode) node);
            case LET_TYPE -> process((BoundLetTypeNode) node);
            case META_INVALID_EXPRESSION -> process((BoundInvalidMetaExpressionNode) node);
            case META_TYPE_EXPRESSION -> process((BoundMetaTypeExpressionNode) node);
            case META_TYPE_OF_EXPRESSION -> process((BoundMetaTypeOfExpressionNode) node);
            case METHOD -> process((BoundMethodNode) node);
            case METHOD_GROUP -> process((BoundMethodGroupExpressionNode) node);
            case METHOD_INVOCATION_EXPRESSION -> process((BoundMethodInvocationExpressionNode) node);
            case NAME_EXPRESSION -> process((BoundNameExpressionNode) node);
            case OBJECT_CREATION_EXPRESSION -> process((BoundObjectCreationExpressionNode) node);
            case OBJECT_INVOCATION -> process((BoundObjectInvocationExpression) node);
            case PARAMETER -> process((BoundParameterNode) node);
            case PARAMETER_LIST -> process((BoundParameterListNode) node);
            case PARENTHESIZED_EXPRESSION -> process((BoundParenthesizedExpressionNode) node);
            case PREDEFINED_TYPE -> process((BoundPredefinedTypeNode) node);
            case PROPERTY -> process((BoundPropertyNode) node);
            case PROPERTY_ACCESS_EXPRESSION -> process((BoundPropertyAccessExpressionNode) node);
            case REF_ARGUMENT_EXPRESSION -> process((BoundRefArgumentExpressionNode) node);
            case REF_TYPE -> process((BoundRefTypeNode) node);
            case RETURN_STATEMENT -> process((BoundReturnStatementNode) node);
            case SET_GENERATOR_BOUNDARY -> process((BoundSetGeneratorBoundaryNode) node);
            case SET_GENERATOR_STATE -> process((BoundSetGeneratorStateNode) node);
            case STACK_LOAD -> process((BoundStackLoadNode) node);
            case STATEMENTS_LIST -> process((BoundStatementsListNode) node);
            case STATIC_REFERENCE -> process((BoundStaticReferenceExpression) node);
            case STATIC_VARIABLE -> process((BoundStaticVariableNode) node);
            case STRING_LITERAL -> process((BoundStringLiteralExpressionNode) node);
            case THIS_EXPRESSION -> process((BoundThisExpressionNode) node);
            case TYPE_CAST_EXPRESSION -> process((BoundTypeCastExpressionNode) node);
            case TYPE_TEST_EXPRESSION -> process((BoundTypeTestExpressionNode) node);
            case UNARY_EXPRESSION -> process((BoundUnaryExpressionNode) node);
            case UNARY_OPERATOR -> process((BoundUnaryOperatorNode) node);
            case UNCONVERTED_LAMBDA -> process((BoundUnconvertedLambdaExpressionNode) node);
            case UNRESOLVED_METHOD -> process((BoundUnresolvedMethodNode) node);
            case VARIABLE_DECLARATION -> process((BoundVariableDeclarationNode) node);
            case VOID_TYPE -> process((BoundVoidTypeNode) node);
            case WHILE_LOOP_STATEMENT -> process(node);
        }
    }

    private void process(BoundArgumentsListNode node) {
        process(node.syntaxNode.openParen);
        process(node.syntaxNode.arguments, node.arguments);
        process(node.syntaxNode.closeParen);
    }

    private void process(BoundArrayCreationExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.typeNode);
        process(node.syntaxNode.openBracket);
        process(node.lengthExpression);
        process(node.syntaxNode.closeBracket);
    }

    private void process(BoundArrayInitializerExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.typeNode);
        process(node.syntaxNode.openBrace);
        process(node.syntaxNode.list, node.items);
        process(node.syntaxNode.closeBrace);
    }

    private void process(BoundArrayTypeNode node) {
        process(node.underlying);
        process(node.syntaxNode.openBracket);
        process(node.syntaxNode.closeBracket);
    }

    private void process(BoundAssignmentOperatorNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundAssignmentStatementNode node) {
        process(node.left);
        process(node.operator);
        process(node.right);
    }

    private void process(BoundAugmentedAssignmentStatementNode node) {
        process(node.left);
        process(node.syntaxNode.operator.token);
        process(node.right);
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundAwaitExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.expression);
    }

    private void process(BoundBinaryExpressionNode node) {
        process(node.left);
        process(node.operator);
        process(node.right);
    }

    private void process(BoundBinaryOperatorNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundBlockStatementNode node) {
        process(node.syntaxNode.openBrace);
        for (BoundStatementNode statement : node.statements) {
            process(statement);
        }
        process(node.syntaxNode.closeBrace);
    }

    private void process(BoundBooleanLiteralExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundBreakStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundCharLiteralExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundClassConstructorNode node) {
        process(node.syntaxNode.keyword);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundClassNode node) {
        process(node.syntaxNode.keyword);
        process(node.name);
        process(node.syntaxNode.openBrace);
        for (BoundClassMemberNode member : node.members) {
            process(member);
        }
        process(node.syntaxNode.closeBrace);
    }

    private void process(BoundClassFieldNode node) {
        process(node.typeNode);
        process(node.name);
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundClassMethodNode node) {
        process(node.syntaxNode.modifiers);
        process(node.typeNode);
        process(node.name);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundCollectionExpressionNode node) {
        process(node.syntaxNode.openBracket);
        process(node.syntaxNode.list, node.list);
        process(node.syntaxNode.closeBracket);
    }

    private void process(BoundCompilationUnitMembersListNode node) {
        for (BoundCompilationUnitMemberNode member : node.members) {
            process(member);
        }
    }

    private void process(BoundCompilationUnitNode node) {
        process(node.members);
        process(node.statements);
        process(node.syntaxNode.end);
    }

    private void process(BoundConditionalExpressionNode node) {
        process(node.condition);
        process(node.syntaxNode.questionMark);
        process(node.whenTrue);
        process(node.syntaxNode.colon);
        process(node.whenFalse);
    }

    private void process(BoundContinueStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundConversionNode node) {
        process(node.expression);
    }

    private void process(BoundCustomTypeNode node) {
        result.add(new SemanticToken(TYPE, node.getRange()));
    }

    private void process(BoundPostfixStatementNode node) {
        process(node.expression);
        process(node.syntaxNode.operation);
        if (node.syntaxNode.semicolon != null) {
            process(node.syntaxNode.semicolon);
        }
    }

    private void process(BoundIndexExpressionNode node) {
        process(node.callee);
        process(node.syntaxNode.openBracket);
        process(node.index);
        process(node.syntaxNode.closeBracket);
    }

    private void process(BoundEmptyCollectionExpressionNode node) {
        process(node.syntaxNode.openBracket);
        process(node.syntaxNode.closeBracket);
    }

    private void process(BoundEmptyStatementNode node) {
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundExpressionStatementNode node) {
        process(node.expression);
        if (node.syntaxNode.semicolon != null) {
            process(node.syntaxNode.semicolon);
        }
    }

    private void process(BoundFloatLiteralExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundForEachLoopStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        process(node.typeNode);
        process(node.name);
        process(node.syntaxNode.in);
        process(node.iterable);
        process(node.syntaxNode.closeParen);
        process(node.body);
    }

    private void process(BoundForLoopStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        if (node.init != null) {
            process(node.init);
        }
        process(node.syntaxNode.semicolon1);
        if (node.condition != null) {
            process(node.condition);
        }
        process(node.syntaxNode.semicolon2);
        if (node.update != null) {
            process(node.update);
        }
        process(node.syntaxNode.closeParen);
        process(node.body);
    }

    private void process(BoundFunctionNode node) {
        process(node.syntaxNode.modifiers);
        process(node.returnType);
        process(node.name);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundFunctionAsLambdaExpressionNode node) {
        process(node.name);
    }

    private void process(BoundFunctionInvocationExpression node) {
        process(node.functionReferenceNode);
        process(node.arguments);
    }

    private void process(BoundFunctionReferenceNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundFunctionTypeNode node) {
        result.add(new SemanticToken(KEYWORD, node.syntaxNode.fn.getRange()));
        result.add(new SemanticToken(BRACKET, node.syntaxNode.openBracket.getRange()));
        if (node.syntaxNode.openParen != null) {
            process(node.syntaxNode.openParen);
        }
        process(node.syntaxNode.parameterTypes, node.parameterTypeNodes);
        if (node.syntaxNode.closeParen != null) {
            process(node.syntaxNode.closeParen);
        }
        process(node.syntaxNode.arrow);
        process(node.returnTypeNode);
        result.add(new SemanticToken(BRACKET, node.syntaxNode.closeBracket.getRange()));
    }

    private void process(BoundGeneratorContinueNode node) {
        throw new InternalException();
    }

    private void process(BoundGeneratorGetValueNode node) {
        throw new InternalException();
    }

    private void process(BoundGeneratorReturnNode node) {
        throw new InternalException();
    }

    private void process(BoundIfStatementNode node) {
        process(node.syntaxNode.ifToken);
        process(node.syntaxNode.openParen);
        process(node.condition);
        process(node.syntaxNode.closeParen);
        process(node.thenStatement);
        if (node.syntaxNode.elseToken != null) {
            process(node.syntaxNode.elseToken);
        }
        if (node.elseStatement != null) {
            process(node.elseStatement);
        }
    }

    private void process(BoundImplicitCastExpressionNode node) {
        process(node.operand);
    }

    private void process(BoundInteger64LiteralExpressionNode node) {
        if (node.syntaxNode.sign != null) {
            process(node.syntaxNode.sign);
        }
        process(node.syntaxNode.token);
    }

    private void process(BoundIntegerLiteralExpressionNode node) {
        if (node.syntaxNode.sign != null) {
            process(node.syntaxNode.sign);
        }
        process(node.syntaxNode.token);
    }

    private void process(BoundInvalidExpressionNode node) {
        if (node.syntaxNode != null) {
            processRaw(node.syntaxNode);
        }
        for (BoundExpressionNode expression : node.children) {
            process(expression);
        }
    }

    private void process(BoundInvalidStatementNode node) {}

    private void process(BoundInvalidTypeNode node) {
        if (node.syntaxNode instanceof LetTypeNode let) {
            process(let.token);
            return;
        }

        result.add(new SemanticToken(TYPE, node.getRange()));
    }

    private void process(BoundJavaTypeNode node) {
        result.add(new SemanticToken(TYPE, node.syntaxNode.java.getRange()));
        process(node.syntaxNode.openBracket);
        for (Token token : node.syntaxNode.name.tokens) {
            result.add(new SemanticToken(TYPE, token.getRange()));
        }
        process(node.syntaxNode.closeBracket);
    }

    private void process(BoundLambdaExpressionNode node) {
        process(node.syntaxNode.openParen);
        process(node.syntaxNode.parameters, node.parameters);
        process(node.syntaxNode.closeParen);
        process(node.syntaxNode.arrow);
        process(node.body);
    }

    private void process(BoundLetTypeNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundInvalidMetaExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundMetaTypeExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        process(node.type);
        process(node.syntaxNode.closeParen);
    }

    private void process(BoundMetaTypeOfExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        process(node.expression);
        process(node.syntaxNode.closeParen);
    }

    private void process(BoundMethodNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundMethodGroupExpressionNode node) {
        process(node.callee);
        process(node.syntaxNode.dot);
        process(node.method);
    }

    private void process(BoundMethodInvocationExpressionNode node) {
        process(node.objectReference);
        if (node.syntaxNode.callee instanceof MemberAccessExpressionNode memberAccess) {
            process(memberAccess.dot);
        }
        process(node.method);
        process(node.arguments);
    }

    private void process(BoundNameExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundObjectCreationExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.typeNode);
        process(node.arguments);
    }

    private void process(BoundObjectInvocationExpression node) {
        process(node.callee);
        process(node.arguments);
    }

    private void process(BoundParameterNode node) {
        if (node.getTypeNode() != null) {
            process(node.getTypeNode());
        }
        process(node.getName());
    }

    private void process(BoundParameterListNode node) {
        process(node.syntaxNode.openParen);
        process(node.syntaxNode.parameters, node.parameters);
        process(node.syntaxNode.closeParen);
    }

    private void process(BoundParenthesizedExpressionNode node) {
        process(node.syntaxNode.openParen);
        process(node.inner);
        process(node.syntaxNode.closeParen);
    }

    private void process(BoundPredefinedTypeNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundPropertyNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundPropertyAccessExpressionNode node) {
        process(node.callee);
        process(node.syntaxNode.dot);
        process(node.property);
    }

    private void process(BoundRefArgumentExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.name);
    }

    private void process(BoundRefTypeNode node) {
        process(node.syntaxNode.keyword);
        process(node.underlying);
    }

    private void process(BoundReturnStatementNode node) {
        process(node.syntaxNode.keyword);
        if (node.expression != null) {
            process(node.expression);
        }
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundSetGeneratorBoundaryNode node) {
        throw new InternalException();
    }

    private void process(BoundSetGeneratorStateNode node) {
        throw new InternalException();
    }

    private void process(BoundStackLoadNode node) {
        throw new InternalException();
    }

    private void process(BoundStatementsListNode node) {
        for (BoundStatementNode statement : node.statements) {
            process(statement);
        }
    }

    private void process(BoundStaticReferenceExpression node) {
        process(node.typeNode);
    }

    private void process(BoundStaticVariableNode node) {
        process(node.syntaxNode.keyword);
        process(node.type);
        process(node.name);
        if (node.syntaxNode.equal != null) {
            process(node.syntaxNode.equal);
        }
        if (node.expression != null) {
            process(node.expression);
        }
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundStringLiteralExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundThisExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundTypeCastExpressionNode node) {
        process(node.expression);
        process(node.syntaxNode.keyword);
        process(node.type);
    }

    private void process(BoundTypeTestExpressionNode node) {
        process(node.expression);
        process(node.syntaxNode.keyword);
        process(node.type);
    }

    private void process(BoundUnaryExpressionNode node) {
        process(node.operator);
        process(node.operand);
    }

    private void process(BoundUnaryOperatorNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundUnconvertedLambdaExpressionNode node) {
        processRaw(node.lambda);
    }

    private void process(BoundUnresolvedMethodNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundVariableDeclarationNode node) {
        process(node.type);
        process(node.name);
        if (node.syntaxNode.equal != null) {
            process(node.syntaxNode.equal);
        }
        if (node.expression != null) {
            process(node.expression);
        }
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundVoidTypeNode node) {
        process(node.syntaxNode.token);
    }

    private void processRaw(ParserNode node) {
        for (Locatable locatable : node.getChildNodes()) {
            if (locatable instanceof Token token) {
                process(token);
                continue;
            }
            if (locatable instanceof ParserNode child) {
                processRaw(child);
                continue;
            }
            throw new InternalException();
        }
    }

    private <TParserNode extends ParserNode, TBoundNode extends BoundNode> void process(SeparatedList<TParserNode> list, List<TBoundNode> boundNodes) {
        List<TParserNode> syntaxNodes = list.getNodes();
        List<Token> separators = list.getCommas();

        if (syntaxNodes.size() != boundNodes.size()) {
            throw new InternalException();
        }

        for (int i = 0; i < syntaxNodes.size(); i++) {
            process(boundNodes.get(i));
            if (i < separators.size()) {
                process(separators.get(i));
            }
        }
    }

    private void process(ModifiersNode node) {
        for (Token token : node.tokens) {
            process(token);
        }
    }

    private void process(Token token) {
        for (Trivia trivia : token.getLeadingTrivia()) {
            process(trivia);
        }

        if (!token.isMissing() && !token.is(TokenType.INVALID)) {
            SemanticTokenType type = switch (token.getTokenType()) {
                case IDENTIFIER -> IDENTIFIER;
                case LEFT_PARENTHESES, LEFT_CURLY_BRACKET, LEFT_SQUARE_BRACKET, RIGHT_PARENTHESES, RIGHT_CURLY_BRACKET,
                     RIGHT_SQUARE_BRACKET -> BRACKET;
                case DOT, DOLLAR, COMMA, SEMICOLON, COLON -> SEPARATOR;
                case PLUS, PLUS_PLUS, PLUS_EQUAL, MINUS, MINUS_MINUS, MINUS_EQUAL, ASTERISK, ASTERISK_EQUAL, SLASH,
                     SLASH_EQUAL, PERCENT, PERCENT_EQUAL, AMPERSAND, AMPERSAND_AMPERSAND, AMPERSAND_EQUAL, PIPE, PIPE_PIPE,
                     PIPE_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EXCLAMATION,
                     EXCLAMATION_EQUAL, QUESTION -> OPERATOR;
                case EQUAL_GREATER -> ARROW;
                case BOOLEAN, INT8, INT16, INT, INT32, INT64, LONG, CHAR, FLOAT32, FLOAT, FLOAT64, STRING, IF, ELSE, BREAK,
                     CONTINUE, WHILE, FOR, FOREACH, FALSE, TRUE, IN, NEW, REF, RETURN, STATIC, VOID, ASYNC, AWAIT, LET, IS,
                     AS, META_UNKNOWN, META_TYPE, META_TYPE_OF, CLASS, CONSTRUCTOR, THIS -> KEYWORD;
                case INTEGER_LITERAL, INTEGER64_LITERAL, FLOAT_LITERAL, INVALID_NUMBER -> NUMBER;
                case CHAR_LITERAL, STRING_LITERAL -> STRING;
                case LINE_BREAK, WHITESPACE, SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT, END_OF_FILE, INVALID -> throw new InternalException();
            };
            result.add(new SemanticToken(type, token.getRange()));
        }

        for (Trivia trivia : token.getTrailingTrivia()) {
            process(trivia);
        }
    }

    private void process(Trivia trivia) {
        if (trivia.is(TokenType.SINGLE_LINE_COMMENT)) {
            result.add(new SemanticToken(COMMENT, trivia.getRange()));
        }
        if (trivia.is(TokenType.MULTI_LINE_COMMENT)) {
            TextRange range = trivia.getRange();
            while (true) {
                if (range instanceof SingleLineTextRange) {
                    result.add(new SemanticToken(COMMENT, range));
                    break;
                }

                Line line = lines.get(range.getLine1() - 1);
                result.add(new SemanticToken(COMMENT,
                        new SingleLineTextRange(
                                range.getLine1(),
                                range.getColumn1(),
                                range.getPosition(),
                                line.length() - (range.getPosition() - line.beginPosition()))));

                range = new MultiLineTextRange(
                        range.getLine1() + 1,
                        1,
                        range.getLine2(),
                        range.getColumn2(),
                        line.endPosition(),
                        range.getLength() - (line.endPosition() - line.beginPosition() - (range.getColumn1() - 1)))
                        .collapse();
            }
        }
    }
}