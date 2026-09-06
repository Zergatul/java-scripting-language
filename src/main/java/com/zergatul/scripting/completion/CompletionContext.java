package com.zergatul.scripting.completion;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.nodes.CustomTypeNode;

import java.util.List;

import static com.zergatul.scripting.TextRange.isBetween;

public class CompletionContext {

    public final ContextType type;
    public final SearchEntry entry;
    public final BoundNode prev;
    public final BoundNode next;
    public final int line;
    public final int column;

    private final Lazy<Boolean> canUnitMemberLazy = new Lazy<>(this::canUnitMemberInternal);
    private final Lazy<Boolean> canClassMemberLazy = new Lazy<>(this::canClassMemberInternal);
    private final Lazy<Boolean> canStatementLazy = new Lazy<>(this::canStatementInternal);
    private final Lazy<Boolean> canExpressionLazy = new Lazy<>(this::canExpressionInternal);
    private final Lazy<Boolean> canTypeLazy = new Lazy<>(this::canTypeInternal);
    private final Lazy<Boolean> canVoidLazy = new Lazy<>(this::canVoidInternal);

    public CompletionContext(ContextType type, int line, int column) {
        this.type = type;
        this.entry = null;
        this.prev = null;
        this.next = null;
        this.line = line;
        this.column = column;
    }

    public CompletionContext(SearchEntry entry, int line, int column) {
        this.type = ContextType.WITHIN;
        this.entry = entry;

        BoundNode prev = null;
        BoundNode next = null;
        List<BoundNode> children = entry.node.getChildren();
        for (int i = -1; i < children.size(); i++) {
            if (i < 0 || children.get(i).getRange().isBefore(line, column)) {
                if (i + 1 >= children.size() || children.get(i + 1).getRange().isAfter(line, column)) {
                    prev = i >= 0 ? children.get(i) : null;
                    next = i < children.size() - 1 ? children.get(i + 1) : null;
                    break;
                }
                if (i + 1 >= children.size() || children.get(i + 1).getRange().contains(line, column)) {
                    prev = i >= 0 ? children.get(i) : null;
                    next = i < children.size() - 2 ? children.get(i + 2) : null;
                    break;
                }
            }
        }

        this.prev = prev;
        this.next = next;
        this.line = line;
        this.column = column;
    }

    public static CompletionContext create(BoundCompilationUnitNode unit, int line, int column) {
        SearchEntry entry = find(null, unit, line, column);
        if (entry == null) {
            if (unit.getRange().isAfter(line, column)) {
                return new CompletionContext(
                        unit.members.members.isEmpty() ? ContextType.BEFORE_FIRST_NO_MEMBERS : ContextType.BEFORE_FIRST_WITH_MEMBERS,
                        line, column);
            }
            if (unit.getRange().isBefore(line, column)) {
                if (unit.getRange().endsWith(line, column)) {
                    return getAtLastContext(unit, line, column);
                } else {
                    if (unit.members.isOpen() && unit.statements.statements.isEmpty()) {
                        TextRange last = unit.members.members.get(unit.members.members.size() - 1).getRange();
                        line = last.getLine2();
                        column = last.getColumn2();
                        entry = find(null, unit, line, column);
                        if (entry == null) {
                            throw new InternalException();
                        }
                        return new CompletionContext(entry, line, column);
                    } else if (unit.statements.isOpen()) {
                        TextRange last = unit.statements.statements.get(unit.statements.statements.size() - 1).getRange();
                        line = last.getLine2();
                        column = last.getColumn2();
                        entry = find(null, unit, line, column);
                        if (entry == null) {
                            throw new InternalException();
                        }
                        return new CompletionContext(entry, line, column);
                    } else {
                        return new CompletionContext(
                                unit.statements.statements.isEmpty() ? ContextType.AFTER_LAST_NO_STATEMENTS : ContextType.AFTER_LAST_WITH_STATEMENTS,
                                line, column);
                    }
                }
            }
            return new CompletionContext(ContextType.NO_CODE, line, column);
        } else {
            return new CompletionContext(entry, line, column);
        }
    }

    public CompletionContext up() {
        if (this.type != ContextType.WITHIN) {
            return null;
        }
        if (this.entry == null || this.entry.parent == null) {
            return null;
        }
        return new CompletionContext(this.entry.parent, line, column);
    }

    public boolean canType() {
        return canTypeLazy.value();
    }

    public boolean canVoid() {
        return canVoidLazy.value();
    }

    public boolean canUnitMember() {
        return canUnitMemberLazy.value();
    }

    public boolean canClassMember() {
        return canClassMemberLazy.value();
    }

    public boolean canStatement() {
        return canStatementLazy.value();
    }

    public boolean canExpression() {
        return canExpressionLazy.value();
    }

    public boolean isFunctionBoundary() {
        if (entry == null) {
            return true;
        }
        if (entry.node.is(BoundNodeType.FUNCTION_DECLARATION)) {
            return true;
        }
        if (entry.node.is(BoundNodeType.CLASS_METHOD)) {
            return true;
        }
        if (entry.node.is(BoundNodeType.CLASS_CONSTRUCTOR)) {
            return true;
        }
        return false;
    }

    public CompletionContext closestStatement(BinderOutput output) {
        if (entry == null) {
            if (type == ContextType.AFTER_LAST_NO_STATEMENTS) {
                return null;
            }
            if (type == ContextType.AFTER_LAST_WITH_STATEMENTS) {
                return new CompletionContext(
                        new SearchEntry(
                                new SearchEntry(null, output.unit()),
                                output.unit().statements),
                        line, column);
            }
            return null;
        }

        CompletionContext current = this;
        while (true) {
            if (current.entry.node instanceof BoundStatementNode) {
                return current;
            }
            if (current.entry.node.getNodeType() == BoundNodeType.FUNCTION_DECLARATION) {
                return null;
            }
            if (current.entry.node.getNodeType() == BoundNodeType.CLASS_METHOD) {
                return null;
            }
            if (current.entry.node.getNodeType() == BoundNodeType.CLASS_CONSTRUCTOR) {
                return null;
            }
            current = current.up();
            if (current == null) {
                return null;
            }
        }
    }

    public BoundStatementNode getPreviousStatement(BinderOutput output) {
        CompletionContext context = closestStatement(output);
        if (context == null) {
            return null;
        }

        CompletionContext parent = context.up();
        if (parent.entry == null) {
            return null;
        }

        if (parent.entry.node.is(BoundNodeType.STATEMENTS_LIST)) {
            BoundStatementsListNode list = (BoundStatementsListNode) parent.entry.node;
            for (int i = 1; i < list.statements.size(); i++) {
                if (list.statements.get(i) == context.entry.node) {
                    return list.statements.get(i - 1);
                }
            }
        }

        if (parent.entry.node.is(BoundNodeType.BLOCK_STATEMENT)) {
            BoundBlockStatementNode block = (BoundBlockStatementNode) parent.entry.node;
            for (int i = 1; i < block.statements.size(); i++) {
                if (block.statements.get(i) == context.entry.node) {
                    return block.statements.get(i - 1);
                }
            }
        }

        return null;
    }

    private boolean canUnitMemberInternal() {
        if (type == ContextType.NO_CODE) {
            return true;
        }
        if (type == ContextType.BEFORE_FIRST_NO_MEMBERS || type == ContextType.BEFORE_FIRST_WITH_MEMBERS) {
            return true;
        }
        if (type == ContextType.AFTER_LAST_NO_STATEMENTS) {
            return true;
        }

        if (type == ContextType.WITHIN) {
            if (entry.node.getNodeType() == BoundNodeType.COMPILATION_UNIT) {
                return true;
            }

            // below we check if unfinished first characters, for example "ty<cursor>"
            // on the boundary between unit members and statements
            // may become unit members

            SearchEntry current = entry;
            switch (current.node.getNodeType()) {
                case NAME_EXPRESSION: {
                    SearchEntry parent = current.parent;
                    if (parent.node.getNodeType() != BoundNodeType.EXPRESSION_STATEMENT) {
                        return false;
                    }

                    SearchEntry grandParent = parent.parent;
                    if (grandParent.node.getNodeType() != BoundNodeType.STATEMENTS_LIST) {
                        return false;
                    }

                    BoundStatementsListNode statements = (BoundStatementsListNode) grandParent.node;
                    BoundExpressionStatementNode statement = (BoundExpressionStatementNode) parent.node;
                    if (statements.statements.get(0) != statement) {
                        return false;
                    }

                    // this is first statement, and it is open
                    // meaning it can also be unit node
                    return statement.isOpen();
                }
                case CUSTOM_TYPE:
                case DECLARED_CLASS_TYPE:
                case ALIASED_TYPE:
                case LET_TYPE:
                case INVALID_TYPE: {
                    SearchEntry parent = current.parent;
                    if (parent.node.getNodeType() != BoundNodeType.VARIABLE_DECLARATION) {
                        return false;
                    }

                    SearchEntry grandParent = parent.parent;
                    if (grandParent.node.getNodeType() != BoundNodeType.STATEMENTS_LIST) {
                        return false;
                    }

                    BoundStatementsListNode statements = (BoundStatementsListNode) grandParent.node;
                    BoundVariableDeclarationNode declaration = (BoundVariableDeclarationNode) parent.node;
                    return statements.statements.get(0) == declaration;
                }
                default:
                    return false;
            }
        }

        return false;
    }

    private boolean canClassMemberInternal() {
        if (entry == null) {
            return false;
        }

        if (entry.node.getNodeType() == BoundNodeType.CLASS_DECLARATION) {
            BoundClassNode classNode = (BoundClassNode) entry.node;
            return isBetween(line, column, classNode.syntaxNode.openBrace, classNode.syntaxNode.closeBrace);
        }

        return false;
    }

    private boolean canStatementInternal() {
        if (entry == null) {
            if (type == ContextType.NO_CODE) {
                return true;
            }
            if (type == ContextType.BEFORE_FIRST_NO_MEMBERS) {
                return true;
            }
            if (type == ContextType.AFTER_LAST_NO_STATEMENTS || type == ContextType.AFTER_LAST_WITH_STATEMENTS) {
                return true;
            }
            return false;
        }

        if (type == ContextType.WITHIN && entry.node.getNodeType() == BoundNodeType.COMPILATION_UNIT) {
            return true;
        }

        // handle cases like this:
        // i<cursor>
        if (entry.isSingleWordStatementStart(line, column)) {
            return true;
        }

        switch (entry.node.getNodeType()) {
            case STATEMENTS_LIST:
            case BLOCK_STATEMENT:
                return true;

            case FOR_LOOP_STATEMENT: {
                BoundForLoopStatementNode loop = (BoundForLoopStatementNode) entry.node;
                return TextRange.isBetween(line, column, loop.syntaxNode.closeParen, loop.body);
            }

            case FOREACH_LOOP_STATEMENT: {
                BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) entry.node;
                return TextRange.isBetween(line, column, loop.syntaxNode.closeParen, loop.body);
            }

            case IF_STATEMENT: {
                BoundIfStatementNode ifStatementNode = (BoundIfStatementNode) entry.node;
                if (ifStatementNode.syntaxNode.elseToken != null) {
                    if (ifStatementNode.syntaxNode.elseToken.getRange().isBefore(line, column)) {
                        if (ifStatementNode.elseStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT) {
                            return true;
                        }
                        if (ifStatementNode.elseStatement.getRange().isAfter(line, column)) {
                            return true;
                        }
                    }
                    if (ifStatementNode.syntaxNode.closeParen.getRange().isBefore(line, column) && ifStatementNode.syntaxNode.elseToken.getRange().isAfter(line, column)) {
                        if (ifStatementNode.thenStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT) {
                            return true;
                        }
                        if (ifStatementNode.thenStatement.getRange().isAfter(line, column)) {
                            return true;
                        }
                    }
                } else {
                    if (ifStatementNode.syntaxNode.closeParen.getRange().isBefore(line, column)) {
                        if (ifStatementNode.thenStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT) {
                            return true;
                        }
                        if (ifStatementNode.thenStatement.getRange().isAfter(line, column)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            default: { // handle: <cursor>(expr).method();
                if (entry.node instanceof BoundExpressionNode) {
                    SearchEntry current = entry;
                    while (current.parent != null) {
                        current = current.parent;
                        if (current.node instanceof BoundExpressionStatementNode) {
                            if (current.node.getRange().getLine1() == line && current.node.getRange().getColumn1() == column) {
                                return true;
                            }
                            break;
                        }
                        if (!(current.node instanceof BoundExpressionNode)) {
                            break;
                        }
                    }
                }
                return false;
            }
        }
    }

    private boolean canExpressionInternal() {
        if (entry == null) {
            if (type == ContextType.NO_CODE) {
                return true;
            }
            if (type == ContextType.AFTER_LAST_NO_STATEMENTS || type == ContextType.AFTER_LAST_WITH_STATEMENTS) {
                return true;
            }
            return false;
        }

        switch (entry.node.getNodeType()) {
            case CLASS_METHOD: {
                BoundClassMethodNode methodNode = (BoundClassMethodNode) entry.node;
                if (methodNode.syntaxNode.arrow != null) {
                    return methodNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case CLASS_UNARY_OPERATION: {
                BoundClassUnaryOperationNode operationNode = (BoundClassUnaryOperationNode) entry.node;
                if (operationNode.syntaxNode.arrow != null) {
                    return operationNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case CLASS_BINARY_OPERATION: {
                BoundClassBinaryOperationNode operationNode = (BoundClassBinaryOperationNode) entry.node;
                if (operationNode.syntaxNode.arrow != null) {
                    return operationNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case EXTENSION_METHOD: {
                BoundExtensionMethodNode methodNode = (BoundExtensionMethodNode) entry.node;
                if (methodNode.syntaxNode.arrow != null) {
                    return methodNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case EXTENSION_UNARY_OPERATION: {
                BoundExtensionUnaryOperationNode operationNode = (BoundExtensionUnaryOperationNode) entry.node;
                if (operationNode.syntaxNode.arrow != null) {
                    return operationNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case EXTENSION_BINARY_OPERATION: {
                BoundExtensionBinaryOperationNode operationNode = (BoundExtensionBinaryOperationNode) entry.node;
                if (operationNode.syntaxNode.arrow != null) {
                    return operationNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case STATIC_VARIABLE: {
                BoundStaticVariableNode variableNode = (BoundStaticVariableNode) entry.node;
                if (variableNode.syntaxNode.equal == null) {
                    return false;
                } else {
                    return variableNode.syntaxNode.equal.getRange().isBefore(line, column);
                }
            }
            case IF_STATEMENT: {
                BoundIfStatementNode statement = (BoundIfStatementNode) entry.node;
                // if (<cursor> <condition>
                if (TextRange.isBetween2(line, column, statement.syntaxNode.openParen, statement.condition)) {
                    return true;
                } else {
                    return canStatement();
                }
            }
            case ASSIGNMENT_STATEMENT: {
                BoundAssignmentStatementNode statement = (BoundAssignmentStatementNode) entry.node;
                return statement.operator.getRange().isBefore(line, column);
            }
            case ARGUMENTS_LIST:
            case BINARY_EXPRESSION:
            case IN_EXPRESSION: {
                return true;
            }
            case BINARY_OPERATOR: {
                return entry.node.getRange().isBefore(line, column);
            }
            case UNCONVERTED_LAMBDA: {
                BoundUnconvertedLambdaExpressionNode lambda = (BoundUnconvertedLambdaExpressionNode) entry.node;
                if (lambda.syntaxNode.arrow.getRange().isBefore(line, column)) {
                    return lambda.isOpen() || lambda.syntaxNode.body.getRange().isAfter(line, column);
                } else {
                    return false;
                }
            }
            case LAMBDA_EXPRESSION: {
                BoundLambdaExpressionNode lambda = (BoundLambdaExpressionNode) entry.node;
                return lambda.isOpen() && lambda.syntaxNode.arrow.getRange().isBefore(line, column);
            }
            case META_CAST_EXPRESSION: {
                BoundMetaCastExpressionNode meta = (BoundMetaCastExpressionNode) entry.node;
                return TextRange.isBetween2(line, column, meta.syntaxNode.openParen, meta.syntaxNode.comma);
            }
            case META_TYPE_OF_EXPRESSION: {
                BoundMetaTypeOfExpressionNode meta = (BoundMetaTypeOfExpressionNode) entry.node;
                return TextRange.isBetween(line, column, meta.syntaxNode.openParen, meta.syntaxNode.closeParen);
            }
            case NAME_EXPRESSION: {
                switch (entry.parent.node.getNodeType()) {
                    case VARIABLE_DECLARATION:
                        BoundVariableDeclarationNode declarationNode = (BoundVariableDeclarationNode) entry.parent.node;
                        return declarationNode.name != entry.node;
                    case PARAMETER:
                        return false;
                    default:
                        return true;
                }
            }
            case ASSIGNMENT_OPERATOR: {
                return up().canExpression();
            }
            default: {
                return canStatement();
            }
        }
    }

    private boolean canTypeInternal() {
        if (canUnitMember()) {
            return true;
        }

        if (entry == null) {
            return false;
        }

        switch (entry.node.getNodeType()) {
            case EXTENSION_DECLARATION: {
                BoundExtensionNode extension = (BoundExtensionNode) entry.node;
                if (isBetween(line, column, extension.syntaxNode.openParen, extension.syntaxNode.closeParen)) {
                    return true;
                }
                return TextRange.isBetween(line, column, extension.syntaxNode.openBrace, extension.syntaxNode.closeBrace);
            }
            case STATIC_VARIABLE: {
                BoundStaticVariableNode variable = (BoundStaticVariableNode) entry.node;
                if (variable.type.isMissing()) {
                    return variable.syntaxNode.keyword.getRange().isBefore(line, column);
                } else {
                    return false;
                }
            }
            case FUNCTION_DECLARATION: {
                BoundFunctionDeclarationNode functionNode = (BoundFunctionDeclarationNode) entry.node;
                if (functionNode.syntaxNode.modifiers.getRange().isBefore(line, column)) {
                    if (functionNode.returnType.isMissing() || functionNode.returnType.getRange().getEnd().isAfter(line, column)) {
                        return true;
                    }
                }
                return false;
            }
            case META_CAST_EXPRESSION: {
                BoundMetaCastExpressionNode meta = (BoundMetaCastExpressionNode) entry.node;
                return TextRange.isBetween(line, column, meta.syntaxNode.comma, meta.syntaxNode.closeParen);
            }
            case META_TYPE_EXPRESSION: {
                BoundMetaTypeExpressionNode meta = (BoundMetaTypeExpressionNode) entry.node;
                return TextRange.isBetween(line, column, meta.syntaxNode.openParen, meta.syntaxNode.closeParen);
            }
            case PARAMETER_LIST: {
                BoundParameterListNode parameters = (BoundParameterListNode) entry.node;
                if (isBetween(line, column, parameters.syntaxNode.openParen, parameters.syntaxNode.closeParen)) {
                    if (parameters.parameters.isEmpty()) {
                        return true;
                    }

                    if (isBetween(line, column, parameters.syntaxNode.openParen, parameters.parameters.get(0))) {
                        return true;
                    }

                    for (int i = 1; i < parameters.parameters.size(); i++) {
                        if (isBetween(line, column, parameters.parameters.get(i - 1), parameters.parameters.get(i))) {
                            return true;
                        }
                    }

                    if (isBetween(line, column, parameters.parameters.get(0), parameters.syntaxNode.closeParen)) {
                        return true;
                    }
                }

                return false;
            }
            case CLASS_DECLARATION: {
                return canClassMember();
            }
            case TYPE_ALIAS: {
                BoundTypeAliasNode typeAliasNode = (BoundTypeAliasNode) entry.node;
                return TextRange.isBetween(line, column, typeAliasNode.syntaxNode.equal, typeAliasNode.syntaxNode.semicolon);
            }
            case INVALID_TYPE:
            case PREDEFINED_TYPE:
            case CUSTOM_TYPE:
            case DECLARED_CLASS_TYPE: {
                return entry.parent.node.isNot(BoundNodeType.CLASS_DECLARATION);
            }
            case INVALID_EXPRESSION: {
                BoundInvalidExpressionNode invalidExpression = (BoundInvalidExpressionNode) entry.node;
                if (invalidExpression.syntaxNode != null) {
                    if (invalidExpression.syntaxNode.nodes.size() == 1 && invalidExpression.syntaxNode.nodes.get(0) instanceof Token) {
                        Token token = (Token) invalidExpression.syntaxNode.nodes.get(0);
                        return token.is(TokenType.NEW) && token.getRange().isBefore(line, column);
                    }
                    if (invalidExpression.syntaxNode.nodes.size() == 2 && invalidExpression.syntaxNode.nodes.get(invalidExpression.syntaxNode.nodes.size() - 1) instanceof CustomTypeNode) {
                        CustomTypeNode custom = (CustomTypeNode) invalidExpression.syntaxNode.nodes.get(invalidExpression.syntaxNode.nodes.size() - 1);
                        return custom.getRange().containsOrEnds(line, column);
                    }
                }
                return false;
            }
            default: {
                return canStatement();
            }
        }
    }

    private boolean canVoidInternal() {
        if (canUnitMember()) {
            return true;
        }

        if (entry == null) {
            return false;
        }

        switch (entry.node.getNodeType()) {
            case FUNCTION_DECLARATION: {
                BoundFunctionDeclarationNode functionNode = (BoundFunctionDeclarationNode) entry.node;
                if (functionNode.syntaxNode.modifiers.getRange().isBefore(line, column)) {
                    if (functionNode.returnType.isMissing() || functionNode.returnType.getRange().getEnd().isAfter(line, column)) {
                        return true;
                    }
                }
                return false;
            }
            case CLASS_DECLARATION: {
                return canClassMember();
            }
            case EXTENSION_DECLARATION: {
                BoundExtensionNode extension = (BoundExtensionNode) entry.node;
                return TextRange.isBetween(line, column, extension.syntaxNode.openBrace, extension.syntaxNode.closeBrace);
            }
            default: {
                return false;
            }
        }
    }

    private static CompletionContext getAtLastContext(BoundCompilationUnitNode unit, int line, int column) {
        if (unit.statements.statements.isEmpty()) {
            return new CompletionContext(ContextType.AFTER_LAST_NO_STATEMENTS, line, column);
        }

        SearchEntry root = new SearchEntry(null, unit);
        SearchEntry child = new SearchEntry(root, unit.statements);

        return new CompletionContext(child, line, column);
    }

    private static SearchEntry find(SearchEntry parent, BoundNode node, int line, int column) {
        if (node.getRange().containsOrEnds(line, column) && node.getRange().getLength() > 0) {
            SearchEntry entry = new SearchEntry(parent, node);
            for (BoundNode child : node.getChildren()) {
                if (child.getRange().containsOrEnds(line, column) && child.getRange().getLength() > 0) {
                    return find(entry, child, line, column);
                }
            }
            return entry;
        } else {
            return null;
        }
    }
}