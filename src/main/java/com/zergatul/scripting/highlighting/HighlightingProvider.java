package com.zergatul.scripting.highlighting;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.symbols.StaticFieldConstantStaticVariable;
import com.zergatul.scripting.symbols.StaticVariable;
import com.zergatul.scripting.utility.Lists;

import java.util.ArrayList;
import java.util.List;

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
            case ALIASED_TYPE:
                process((BoundAliasedTypeNode) node);
                break;
            case ARGUMENTS_LIST:
                process((BoundArgumentsListNode) node);
                break;
            case ARRAY_CREATION_EXPRESSION:
                process((BoundArrayCreationExpressionNode) node);
                break;
            case ARRAY_INITIALIZER_EXPRESSION:
                process((BoundArrayInitializerExpressionNode) node);
                break;
            case ARRAY_TYPE:
                process((BoundArrayTypeNode) node);
                break;
            case ASSIGNMENT_OPERATOR:
                process((BoundAssignmentOperatorNode) node);
                break;
            case ASSIGNMENT_STATEMENT:
                process((BoundAssignmentStatementNode) node);
                break;
            case AUGMENTED_ASSIGNMENT_STATEMENT:
                process((BoundAugmentedAssignmentStatementNode) node);
                break;
            case AWAIT_EXPRESSION:
                process((BoundAwaitExpressionNode) node);
                break;
            case BASE_METHOD_INVOCATION_EXPRESSION:
                process((BoundBaseMethodInvocationExpressionNode) node);
                break;
            case BINARY_EXPRESSION:
                process((BoundBinaryExpressionNode) node);
                break;
            case BINARY_OPERATOR:
                process((BoundBinaryOperatorNode) node);
                break;
            case BLOCK_STATEMENT:
                process((BoundBlockStatementNode) node);
                break;
            case BOOLEAN_LITERAL:
                process((BoundBooleanLiteralExpressionNode) node);
                break;
            case BREAK_STATEMENT:
                process((BoundBreakStatementNode) node);
                break;
            case CHAR_LITERAL:
                process((BoundCharLiteralExpressionNode) node);
                break;
            case CLASS_BINARY_OPERATION:
                process((BoundClassBinaryOperationNode) node);
                break;
            case CLASS_CONSTRUCTOR:
                process((BoundClassConstructorNode) node);
                break;
            case CLASS_DECLARATION:
                process((BoundClassNode) node);
                break;
            case CLASS_FIELD:
                process((BoundClassFieldNode) node);
                break;
            case CLASS_METHOD:
                process((BoundClassMethodNode) node);
                break;
            case COLLECTION_EXPRESSION:
                process((BoundCollectionExpressionNode) node);
                break;
            case COMPILATION_UNIT:
                process((BoundCompilationUnitNode) node);
                break;
            case COMPILATION_UNIT_MEMBERS:
                process((BoundCompilationUnitMembersListNode) node);
                break;
            case CONDITIONAL_EXPRESSION:
                process((BoundConditionalExpressionNode) node);
                break;
            case CONSTANT_PATTERN:
                process((BoundConstantPatternNode) node);
                break;
            case CONSTRUCTOR_INITIALIZER:
                process((BoundConstructorInitializerNode) node);
                break;
            case CONTINUE_STATEMENT:
                process((BoundContinueStatementNode) node);
                break;
            case CONVERSION:
                process((BoundConversionNode) node);
                break;
            case CUSTOM_TYPE:
                process((BoundCustomTypeNode) node);
                break;
            case DECLARATION_PATTERN:
                process((BoundDeclarationPatternNode) node);
                break;
            case DECLARED_CLASS_TYPE:
                process((BoundDeclaredClassTypeNode) node);
                break;
            case DECREMENT_STATEMENT:
                process((BoundPostfixStatementNode) node);
                break;
            case EMPTY_COLLECTION_EXPRESSION:
                process((BoundEmptyCollectionExpressionNode) node);
                break;
            case EMPTY_STATEMENT:
                process((BoundEmptyStatementNode) node);
                break;
            case EXPRESSION_STATEMENT:
                process((BoundExpressionStatementNode) node);
                break;
            case EXTENSION_DECLARATION:
                process((BoundExtensionNode) node);
                break;
            case EXTENSION_BINARY_OPERATION:
                process((BoundExtensionBinaryOperationNode) node);
                break;
            case EXTENSION_METHOD:
                process((BoundExtensionMethodNode) node);
                break;
            case EXTENSION_UNARY_OPERATION:
                process((BoundExtensionUnaryOperationNode) node);
                break;
            case FLOAT_LITERAL:
                process((BoundFloatLiteralExpressionNode) node);
                break;
            case FOREACH_LOOP_STATEMENT:
                process((BoundForEachLoopStatementNode) node);
                break;
            case FOR_LOOP_STATEMENT:
                process((BoundForLoopStatementNode) node);
                break;
            case FUNCTION:
                process((BoundFunctionNode) node);
                break;
            case FUNCTION_DECLARATION:
                process((BoundFunctionDeclarationNode) node);
                break;
            case FUNCTION_AS_LAMBDA:
                process((BoundFunctionAsLambdaExpressionNode) node);
                break;
            case FUNCTION_INVOCATION:
                process((BoundFunctionInvocationExpression) node);
                break;
            case FUNCTION_TYPE:
                process((BoundFunctionTypeNode) node);
                break;
            case GENERATOR_CONTINUE:
                process((BoundGeneratorContinueNode) node);
                break;
            case GENERATOR_GET_VALUE:
                process((BoundGeneratorGetValueNode) node);
                break;
            case GENERATOR_RETURN:
                process((BoundGeneratorReturnNode) node);
                break;
            case IF_STATEMENT:
                process((BoundIfStatementNode) node);
                break;
            case IMPLICIT_CAST:
                process((BoundImplicitCastExpressionNode) node);
                break;
            case INCREMENT_STATEMENT:
                process((BoundPostfixStatementNode) node);
                break;
            case INDEX_EXPRESSION:
                process((BoundIndexExpressionNode) node);
                break;
            case INTEGER64_LITERAL:
                process((BoundInteger64LiteralExpressionNode) node);
                break;
            case INTEGER_LITERAL:
                process((BoundIntegerLiteralExpressionNode) node);
                break;
            case INVALID_EXPRESSION:
                process((BoundInvalidExpressionNode) node);
                break;
            case INVALID_STATEMENT:
                process((BoundInvalidStatementNode) node);
                break;
            case INVALID_TYPE:
                process((BoundInvalidTypeNode) node);
                break;
            case JAVA_TYPE:
                process((BoundJavaTypeNode) node);
                break;
            case LAMBDA_EXPRESSION:
                process((BoundLambdaExpressionNode) node);
                break;
            case LET_TYPE:
                process((BoundLetTypeNode) node);
                break;
            case META_CAST_EXPRESSION:
                process((BoundMetaCastExpressionNode) node);
                break;
            case META_INVALID_EXPRESSION:
                process((BoundInvalidMetaExpressionNode) node);
                break;
            case META_TYPE_EXPRESSION:
                process((BoundMetaTypeExpressionNode) node);
                break;
            case META_TYPE_OF_EXPRESSION:
                process((BoundMetaTypeOfExpressionNode) node);
                break;
            case METHOD:
                process((BoundMethodNode) node);
                break;
            case METHOD_GROUP:
                process((BoundMethodGroupExpressionNode) node);
                break;
            case METHOD_INVOCATION_EXPRESSION:
                process((BoundMethodInvocationExpressionNode) node);
                break;
            case NAME_EXPRESSION:
                process((BoundNameExpressionNode) node);
                break;
            case NOT_PATTERN:
                process((BoundNotPattern) node);
                break;
            case NULL_EXPRESSION:
                process((BoundNullExpressionNode) node);
                break;
            case OBJECT_CREATION_EXPRESSION:
                process((BoundObjectCreationExpressionNode) node);
                break;
            case OBJECT_INVOCATION:
                process((BoundObjectInvocationExpression) node);
                break;
            case PARAMETER:
                process((BoundParameterNode) node);
                break;
            case PARAMETER_LIST:
                process((BoundParameterListNode) node);
                break;
            case PARENTHESIZED_EXPRESSION:
                process((BoundParenthesizedExpressionNode) node);
                break;
            case PREDEFINED_TYPE:
                process((BoundPredefinedTypeNode) node);
                break;
            case PROPERTY:
                process((BoundPropertyNode) node);
                break;
            case PROPERTY_ACCESS_EXPRESSION:
                process((BoundPropertyAccessExpressionNode) node);
                break;
            case REF_ARGUMENT_EXPRESSION:
                process((BoundRefArgumentExpressionNode) node);
                break;
            case REF_TYPE:
                process((BoundRefTypeNode) node);
                break;
            case RETURN_STATEMENT:
                process((BoundReturnStatementNode) node);
                break;
            case SET_GENERATOR_STATE:
                process((BoundSetGeneratorStateNode) node);
                break;
            case STACK_LOAD:
                process((BoundStackLoadNode) node);
                break;
            case STATEMENTS_LIST:
                process((BoundStatementsListNode) node);
                break;
            case STATIC_REFERENCE:
                process((BoundStaticReferenceExpression) node);
                break;
            case STATIC_VARIABLE:
                process((BoundStaticVariableNode) node);
                break;
            case STRING_LITERAL:
                process((BoundStringLiteralExpressionNode) node);
                break;
            case SYMBOL:
                process((BoundSymbolNode) node);
                break;
            case THIS_EXPRESSION:
                process((BoundThisExpressionNode) node);
                break;
            case TRY_STATEMENT:
                process((BoundTryStatementNode) node);
                break;
            case TYPE_ALIAS:
                process((BoundTypeAliasNode) node);
                break;
            case TYPE_CAST_EXPRESSION:
                process((BoundTypeCastExpressionNode) node);
                break;
            case TYPE_PATTERN:
                process((BoundTypePatternNode) node);
                break;
            case IS_EXPRESSION:
                process((BoundIsExpressionNode) node);
                break;
            case UNARY_EXPRESSION:
                process((BoundUnaryExpressionNode) node);
                break;
            case UNARY_OPERATOR:
                process((BoundUnaryOperatorNode) node);
                break;
            case UNCONVERTED_LAMBDA:
                process((BoundUnconvertedLambdaExpressionNode) node);
                break;
            case UNRESOLVED_METHOD:
                process((BoundUnresolvedMethodNode) node);
                break;
            case VARIABLE_DECLARATION:
                process((BoundVariableDeclarationNode) node);
                break;
            case VOID_TYPE:
                process((BoundVoidTypeNode) node);
                break;
            case WHILE_LOOP_STATEMENT:
                process((BoundWhileLoopStatementNode) node);
                break;
        }
    }

    private void process(BoundAliasedTypeNode node) {
        process(node.syntaxNode.token, SemanticTokenType.TYPE);
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
        if (node.syntaxNode.semicolon != null) {
            process(node.syntaxNode.semicolon);
        }
    }

    private void process(BoundAugmentedAssignmentStatementNode node) {
        process(node.left);
        process(node.syntaxNode.operator.token);
        process(node.right);
        if (node.syntaxNode.semicolon != null) {
            process(node.syntaxNode.semicolon);
        }
    }

    private void process(BoundAwaitExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.expression);
    }

    private void process(BoundBaseMethodInvocationExpressionNode node) {
        process(node.getBaseExpressionSyntaxNode().token);
        process(((MemberAccessExpressionNode) node.syntaxNode.callee).operator);
        process(node.method);
        process(node.arguments);
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

    private void process(BoundClassBinaryOperationNode node) {
        process(node.syntaxNode.keyword, SemanticTokenType.KEYWORD);
        process(node.syntaxNode.openBracket);
        process(node.syntaxNode.operator);
        process(node.syntaxNode.closeBracket);
        process(node.returnTypeNode);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundClassConstructorNode node) {
        process(node.syntaxNode.modifiers);
        process(node.syntaxNode.keyword);
        process(node.parameters);
        if (node.syntaxNode.colon != null) {
            process(node.syntaxNode.colon);
        }
        if (node.syntaxNode.initializer != null) {
            process(node.initializer);
        }
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundClassNode node) {
        process(node.syntaxNode.keyword);
        process(node.name.syntaxNode.token, SemanticTokenType.TYPE);
        if (node.syntaxNode.colon != null) {
            process(node.syntaxNode.colon);
        }
        process(node.syntaxNode.baseTypeNodes, node.baseTypeNodes);
        process(node.syntaxNode.openBrace);
        for (BoundClassMemberNode member : node.members) {
            process(member);
        }
        process(node.syntaxNode.closeBrace);
    }

    private void process(BoundClassFieldNode node) {
        process(node.syntaxNode.modifiers);
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

    private void process(BoundConstantPatternNode node) {
        process(node.expression);
    }

    private void process(BoundConstructorInitializerNode node) {
        process(node.syntaxNode.keyword);
        process(node.arguments);
    }

    private void process(BoundContinueStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundConversionNode node) {
        process(node.expression);
    }

    private void process(BoundCustomTypeNode node) {
        process(node.syntaxNode.token, SemanticTokenType.TYPE);
    }

    private void process(BoundDeclarationPatternNode node) {
        process(node.typeNode);
        process(node.symbolNode);
    }

    private void process(BoundDeclaredClassTypeNode node) {
        process(node.syntaxNode.token, SemanticTokenType.TYPE);
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

    private void process(BoundExtensionNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        process(node.typeNode);
        process(node.syntaxNode.closeParen);
        process(node.syntaxNode.openBrace);
        for (BoundExtensionMemberNode member : node.members) {
            process(member);
        }
        process(node.syntaxNode.closeBrace);
    }

    private void process(BoundExtensionBinaryOperationNode node) {
        process(node.syntaxNode.keyword, SemanticTokenType.KEYWORD);
        process(node.syntaxNode.openBracket);
        process(node.syntaxNode.operator);
        process(node.syntaxNode.closeBracket);
        process(node.returnTypeNode);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundExtensionMethodNode node) {
        process(node.syntaxNode.modifiers);
        process(node.typeNode);
        process(node.name);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
    }

    private void process(BoundExtensionUnaryOperationNode node) {
        process(node.syntaxNode.keyword, SemanticTokenType.KEYWORD);
        process(node.syntaxNode.openBracket);
        process(node.syntaxNode.operator);
        process(node.syntaxNode.closeBracket);
        process(node.returnTypeNode);
        process(node.parameters);
        if (node.syntaxNode.arrow != null) {
            process(node.syntaxNode.arrow);
        }
        process(node.body);
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
        process(node.syntaxNode.token, SemanticTokenType.IDENTIFIER, Lists.of(SemanticTokenModifier.FUNCTION));
    }

    private void process(BoundFunctionDeclarationNode node) {
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
        process(node.functionNode);
        process(node.arguments);
    }

    private void process(BoundFunctionTypeNode node) {
        process(node.syntaxNode.fn, SemanticTokenType.KEYWORD);
        process(node.syntaxNode.openBracket, SemanticTokenType.BRACKET);
        if (node.syntaxNode.openParen != null) {
            process(node.syntaxNode.openParen);
        }
        process(node.syntaxNode.parameterTypes, node.parameterTypeNodes);
        if (node.syntaxNode.closeParen != null) {
            process(node.syntaxNode.closeParen);
        }
        process(node.syntaxNode.arrow);
        process(node.returnTypeNode);
        process(node.syntaxNode.closeBracket, SemanticTokenType.BRACKET);
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
        for (ParserNode syntaxNode : node.unboundNodes) {
            processRaw(syntaxNode);
        }
    }

    private void process(BoundInvalidStatementNode node) {}

    private void process(BoundInvalidTypeNode node) {
        if (node.syntaxNode instanceof LetTypeNode) {
            LetTypeNode let = (LetTypeNode) node.syntaxNode;
            process(let.token);
            return;
        }
        if (node.syntaxNode instanceof CustomTypeNode) {
            CustomTypeNode custom = (CustomTypeNode) node.syntaxNode;
            process(custom.token, SemanticTokenType.TYPE);
            return;
        }
        if (node.syntaxNode instanceof InvalidTypeNode) {
            InvalidTypeNode invalid = (InvalidTypeNode) node.syntaxNode;
            process(invalid.token, SemanticTokenType.TYPE);
            return;
        }
    }

    private void process(BoundJavaTypeNode node) {
        process(node.syntaxNode.java, SemanticTokenType.TYPE);
        process(node.syntaxNode.openBracket);
        for (Token token : node.syntaxNode.name.tokens) {
            process(token, SemanticTokenType.TYPE);
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

    private void process(BoundMetaCastExpressionNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        process(node.expression);
        process(node.syntaxNode.comma);
        process(node.type);
        process(node.syntaxNode.closeParen);
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
        process(node.syntaxNode.token, SemanticTokenType.METHOD);
    }

    private void process(BoundMethodGroupExpressionNode node) {
        process(node.callee);
        process(node.syntaxNode.operator);
        process(node.method);
    }

    private void process(BoundMethodInvocationExpressionNode node) {
        process(node.objectReference);
        if (node.syntaxNode.callee instanceof MemberAccessExpressionNode) {
            MemberAccessExpressionNode memberAccess = (MemberAccessExpressionNode) node.syntaxNode.callee;
            process(memberAccess.operator);
        }
        process(node.method);
        process(node.arguments);
    }

    private void process(BoundNameExpressionNode node) {
        if (node.getSymbol() instanceof StaticFieldConstantStaticVariable) {
            process(node.syntaxNode.token, SemanticTokenType.IDENTIFIER, Lists.of(SemanticTokenModifier.EXTERNAL, SemanticTokenModifier.STATIC));
            return;
        }
        if (node.getSymbol() instanceof StaticVariable) {
            process(node.syntaxNode.token, SemanticTokenType.IDENTIFIER, Lists.of(SemanticTokenModifier.STATIC));
            return;
        }
        if (node.getSymbol() instanceof Function) {
            process(node.syntaxNode.token, SemanticTokenType.IDENTIFIER, Lists.of(SemanticTokenModifier.FUNCTION));
            return;
        }
        process(node.syntaxNode.token);
    }

    private void process(BoundNotPattern pattern) {
        process(pattern.syntaxNode.keyword, SemanticTokenType.KEYWORD);
        process(pattern.inner);
    }

    private void process(BoundNullExpressionNode expression) {
        process(expression.syntaxNode.token);
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
        process(node.syntaxNode.token, SemanticTokenType.PROPERTY);
    }

    private void process(BoundPropertyAccessExpressionNode node) {
        process(node.callee);
        process(node.syntaxNode.operator);
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

    private void process(BoundSymbolNode node) {
        //if (node.symbolRef.get() instanceof Variable) {
        // TODO: other cases? like BoundNameExpression
        process(node.token);
        //}
    }

    private void process(BoundThisExpressionNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundTryStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.block);
        if (node.syntaxNode.catchClause != null) {
            process(node.syntaxNode.catchClause.keyword);
            if (node.syntaxNode.catchClause.declaration != null) {
                process(node.syntaxNode.catchClause.declaration.openParen);
                if (node.exceptionSymbol != null) {
                    process(node.exceptionSymbol);
                }
                process(node.syntaxNode.catchClause.declaration.closeParen);
            }
        }
        if (node.catchBlock != null) {
            process(node.catchBlock);
        }
        if (node.syntaxNode.finallyClause != null) {
            process(node.syntaxNode.finallyClause.keyword);
        }
        if (node.finallyBlock != null) {
            process(node.finallyBlock);
        }
    }

    private void process(BoundTypeAliasNode node) {
        process(node.syntaxNode.keyword);
        process(node.name.token, SemanticTokenType.TYPE);
        process(node.syntaxNode.equal);
        process(node.typeNode);
        process(node.syntaxNode.semicolon);
    }

    private void process(BoundTypeCastExpressionNode node) {
        process(node.expression);
        process(node.syntaxNode.keyword);
        process(node.type);
    }

    private void process(BoundTypePatternNode node) {
        process(node.typeNode);
    }

    private void process(BoundIsExpressionNode node) {
        process(node.expression);
        process(node.syntaxNode.keyword);
        process(node.pattern);
    }

    private void process(BoundUnaryExpressionNode node) {
        process(node.operator);
        process(node.operand);
    }

    private void process(BoundUnaryOperatorNode node) {
        process(node.syntaxNode.token);
    }

    private void process(BoundUnconvertedLambdaExpressionNode node) {
        processRaw(node.syntaxNode);
    }

    private void process(BoundUnresolvedMethodNode node) {
        process(node.syntaxNode.token, SemanticTokenType.METHOD);
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

    private void process(BoundWhileLoopStatementNode node) {
        process(node.syntaxNode.keyword);
        process(node.syntaxNode.openParen);
        process(node.condition);
        process(node.syntaxNode.closeParen);
        process(node.body);
    }

    private void processRaw(ParserNode node) {
        if (node.is(ParserNodeType.CUSTOM_TYPE)) {
            CustomTypeNode custom = (CustomTypeNode) node;
            process(custom.token, SemanticTokenType.TYPE);
            return;
        }

        for (Locatable locatable : node.getChildNodes()) {
            if (locatable instanceof Token) {
                Token token = (Token) locatable;
                process(token);
                continue;
            }
            if (locatable instanceof ParserNode) {
                ParserNode child = (ParserNode) locatable;
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
            SemanticTokenType type;
            switch (token.getTokenType()) {
                case IDENTIFIER:
                    type = SemanticTokenType.IDENTIFIER;
                    break;
                case LEFT_PARENTHESES:
                case LEFT_CURLY_BRACKET:
                case LEFT_SQUARE_BRACKET:
                case RIGHT_PARENTHESES:
                case RIGHT_CURLY_BRACKET:
                case RIGHT_SQUARE_BRACKET:
                    type = SemanticTokenType.BRACKET;
                    break;
                case DOT:
                case DOT_HASH:
                case DOLLAR:
                case COMMA:
                case SEMICOLON:
                case COLON:
                    type = SemanticTokenType.SEPARATOR;
                    break;
                case PLUS:
                case PLUS_PLUS:
                case PLUS_EQUAL:
                case MINUS:
                case MINUS_MINUS:
                case MINUS_EQUAL:
                case ASTERISK:
                case ASTERISK_EQUAL:
                case SLASH:
                case SLASH_EQUAL:
                case PERCENT:
                case PERCENT_EQUAL:
                case AMPERSAND:
                case AMPERSAND_AMPERSAND:
                case AMPERSAND_EQUAL:
                case PIPE:
                case PIPE_PIPE:
                case PIPE_EQUAL:
                case EQUAL:
                case EQUAL_EQUAL:
                case GREATER:
                case GREATER_EQUAL:
                case LESS:
                case LESS_EQUAL:
                case EXCLAMATION:
                case EXCLAMATION_EQUAL:
                case QUESTION:
                case EQUAL_GREATER:
                case QUESTION_QUESTION:
                case QUESTION_QUESTION_EQUAL:
                    type = SemanticTokenType.OPERATOR;
                    break;
                case BOOLEAN:
                case INT8:
                case INT16:
                case INT:
                case INT32:
                case INT64:
                case LONG:
                case CHAR:
                case FLOAT32:
                case FLOAT:
                case FLOAT64:
                case STRING:
                case IF:
                case ELSE:
                case BREAK:
                case CONTINUE:
                case WHILE:
                case FOR:
                case FOREACH:
                case FALSE:
                case TRUE:
                case IN:
                case NEW:
                case REF:
                case RETURN:
                case STATIC:
                case VOID:
                case ASYNC:
                case AWAIT:
                case LET:
                case IS:
                case AS:
                case META_UNKNOWN:
                case META_CAST:
                case META_TYPE:
                case META_TYPE_OF:
                case CLASS:
                case CONSTRUCTOR:
                case THIS:
                case EXTENSION:
                case ABSTRACT:
                case VIRTUAL:
                case OVERRIDE:
                case PUBLIC:
                case PROTECTED:
                case PRIVATE:
                case BASE:
                case TYPEALIAS:
                case NULL:
                case TRY:
                case CATCH:
                case FINALLY:
                case THROW:
                    type = SemanticTokenType.KEYWORD;
                    break;
                case INTEGER_LITERAL:
                case INTEGER64_LITERAL:
                case FLOAT_LITERAL:
                case INVALID_NUMBER:
                    type = SemanticTokenType.NUMBER;
                    break;
                case CHAR_LITERAL:
                case STRING_LITERAL:
                    type = SemanticTokenType.STRING;
                    break;
                case LINE_BREAK:
                case WHITESPACE:
                case SINGLE_LINE_COMMENT:
                case MULTI_LINE_COMMENT:
                case END_OF_FILE:
                case INVALID:
                    throw new InternalException();
                default:
                    throw new InternalException();
            }

            List<SemanticTokenModifier> modifiers;
            switch (token.getTokenType()) {
                case VOID:
                case BOOLEAN:
                case INT8:
                case INT16:
                case INT:
                case INT32:
                case INT64:
                case LONG:
                case CHAR:
                case FLOAT32:
                case FLOAT:
                case FLOAT64:
                case STRING:
                    modifiers = Lists.of(SemanticTokenModifier.PREDEFINED_TYPE);
                    break;
                case AS:
                case IS:
                case NEW:
                case AWAIT:
                    modifiers = Lists.of(SemanticTokenModifier.OPERATOR_LIKE);
                    break;
                case ASYNC:
                    modifiers = Lists.of(SemanticTokenModifier.ASYNC);
                    break;
                case FALSE:
                case TRUE:
                    modifiers = Lists.of(SemanticTokenModifier.VALUE);
                    break;
                default:
                    modifiers = Lists.of();
                    break;
            }

            result.add(new SemanticToken(type, modifiers, token.getRange()));
        }

        for (Trivia trivia : token.getTrailingTrivia()) {
            process(trivia);
        }
    }

    private void process(Token token, SemanticTokenType type) {
        process(token, type, Lists.of());
    }

    private void process(Token token, SemanticTokenType type, List<SemanticTokenModifier> modifiers) {
        for (Trivia trivia : token.getLeadingTrivia()) {
            process(trivia);
        }

        result.add(new SemanticToken(type, modifiers, token.getRange()));

        for (Trivia trivia : token.getTrailingTrivia()) {
            process(trivia);
        }
    }

    private void process(Trivia trivia) {
        if (trivia.is(TokenType.SINGLE_LINE_COMMENT)) {
            result.add(new SemanticToken(SemanticTokenType.COMMENT, trivia.getRange()));
        }
        if (trivia.is(TokenType.MULTI_LINE_COMMENT)) {
            TextRange range = trivia.getRange();
            while (true) {
                if (range instanceof SingleLineTextRange) {
                    result.add(new SemanticToken(SemanticTokenType.COMMENT, range));
                    break;
                }

                Line line = lines.get(range.getLine1() - 1);
                result.add(new SemanticToken(SemanticTokenType.COMMENT,
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