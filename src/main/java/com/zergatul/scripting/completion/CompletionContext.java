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
                        TextRange last = unit.members.members.getLast().getRange();
                        line = last.getLine2();
                        column = last.getColumn2();
                        entry = find(null, unit, line, column);
                        if (entry == null) {
                            throw new InternalException();
                        }
                        return new CompletionContext(entry, line, column);
                    } else if (unit.statements.isOpen()) {
                        TextRange last = unit.statements.statements.getLast().getRange();
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
            if (current.entry.node.getNodeType() == BoundNodeType.FUNCTION) {
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
            return entry.node.getNodeType() == BoundNodeType.COMPILATION_UNIT;
        }
        return false;
    }

    private boolean canClassMemberInternal() {
        if (entry == null) {
            return false;
        }

        if (entry.node.getNodeType() == BoundNodeType.CLASS_DECLARATION) {
            BoundClassNode classNode = (BoundClassNode) entry.node;
            return TextRange.isBetween(line, column, classNode.syntaxNode.openBrace, classNode.syntaxNode.closeBrace);
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
        return switch (entry.node.getNodeType()) {

            case STATEMENTS_LIST, BLOCK_STATEMENT -> true;

            case FOR_LOOP_STATEMENT -> {
                BoundForLoopStatementNode loop = (BoundForLoopStatementNode) entry.node;
                yield TextRange.isBetween(line, column, loop.syntaxNode.closeParen, loop.body);
            }

            case FOREACH_LOOP_STATEMENT -> {
                BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) entry.node;
                yield TextRange.isBetween(line, column, loop.syntaxNode.closeParen, loop.body);
            }

            case IF_STATEMENT -> {
                BoundIfStatementNode ifStatementNode = (BoundIfStatementNode) entry.node;
                if (ifStatementNode.syntaxNode.elseToken != null) {
                    if (ifStatementNode.syntaxNode.elseToken.getRange().isBefore(line, column)) {
                        if (ifStatementNode.elseStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT) {
                            yield true;
                        }
                        if (ifStatementNode.elseStatement.getRange().isAfter(line, column)) {
                            yield true;
                        }
                    }
                    if (ifStatementNode.syntaxNode.closeParen.getRange().isBefore(line, column) && ifStatementNode.syntaxNode.elseToken.getRange().isAfter(line, column)) {
                        if (ifStatementNode.thenStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT) {
                            yield true;
                        }
                        if (ifStatementNode.thenStatement.getRange().isAfter(line, column)) {
                            yield true;
                        }
                    }
                } else {
                    if (ifStatementNode.syntaxNode.closeParen.getRange().isBefore(line, column)) {
                        if (ifStatementNode.thenStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT) {
                            yield true;
                        }
                        if (ifStatementNode.thenStatement.getRange().isAfter(line, column)) {
                            yield true;
                        }
                    }
                }
                yield false;
            }

            default -> {
                // handle: <cursor>(expr).method();
                if (entry.node instanceof BoundExpressionNode) {
                    SearchEntry current = entry;
                    while (current.parent != null) {
                        current = current.parent;
                        if (current.node instanceof BoundExpressionStatementNode) {
                            if (current.node.getRange().getLine1() == line && current.node.getRange().getColumn1() == column) {
                                yield true;
                            }
                            break;
                        }
                        if (!(current.node instanceof BoundExpressionNode)) {
                            break;
                        }
                    }
                }
                yield false;
            }
        };
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

        return switch (entry.node.getNodeType()) {

            case CLASS_METHOD -> {
                BoundClassMethodNode methodNode = (BoundClassMethodNode) entry.node;
                if (methodNode.syntaxNode.arrow != null) {
                    yield methodNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    yield false;
                }
            }

            case EXTENSION_METHOD -> {
                BoundExtensionMethodNode methodNode = (BoundExtensionMethodNode) entry.node;
                if (methodNode.syntaxNode.arrow != null) {
                    yield methodNode.syntaxNode.arrow.getRange().isBefore(line, column);
                } else {
                    yield false;
                }
            }

            case STATIC_VARIABLE -> {
                BoundStaticVariableNode variableNode = (BoundStaticVariableNode) entry.node;
                if (variableNode.syntaxNode.equal == null) {
                    yield false;
                } else {
                    yield variableNode.syntaxNode.equal.getRange().isBefore(line, column);
                }
            }

            case IF_STATEMENT -> {
                BoundIfStatementNode statement = (BoundIfStatementNode) entry.node;
                // if (<cursor> <condition>
                if (TextRange.isBetween2(line, column, statement.syntaxNode.openParen, statement.condition)) {
                    yield true;
                } else {
                    yield canStatement();
                }
            }
            case ASSIGNMENT_STATEMENT -> {
                BoundAssignmentStatementNode statement = (BoundAssignmentStatementNode) entry.node;
                yield statement.operator.getRange().isBefore(line, column);
            }

            case ARGUMENTS_LIST, BINARY_EXPRESSION, IN_EXPRESSION -> true;

            case BINARY_OPERATOR -> entry.node.getRange().isBefore(line, column);

            case LAMBDA_EXPRESSION -> {
                BoundLambdaExpressionNode lambda = (BoundLambdaExpressionNode) entry.node;
                yield lambda.isOpen() && lambda.syntaxNode.arrow.getRange().isBefore(line, column);
            }

            case META_TYPE_OF_EXPRESSION -> {
                BoundMetaTypeOfExpressionNode meta = (BoundMetaTypeOfExpressionNode) entry.node;
                yield TextRange.isBetween(line, column, meta.syntaxNode.openParen, meta.syntaxNode.closeParen);
            }

            case NAME_EXPRESSION -> {
                yield switch (entry.parent.node.getNodeType()) {
                    case VARIABLE_DECLARATION -> {
                        BoundVariableDeclarationNode declarationNode = (BoundVariableDeclarationNode) entry.parent.node;
                        yield declarationNode.name != entry.node;
                    }
                    case PARAMETER -> false;
                    default -> true;
                };
            }

            case ASSIGNMENT_OPERATOR -> {
                yield up().canExpression();
            }

            default -> canStatement();
        };
    }

    private boolean canTypeInternal() {
        if (canUnitMember()) {
            return true;
        }

        if (entry == null) {
            return false;
        }

        return switch (entry.node.getNodeType()) {
            case EXTENSION_DECLARATION -> {
                BoundExtensionNode extension = (BoundExtensionNode) entry.node;
                if (TextRange.isBetween(line, column, extension.syntaxNode.openParen, extension.syntaxNode.closeParen)) {
                    yield true;
                }
                yield TextRange.isBetween(line, column, extension.syntaxNode.openBrace, extension.syntaxNode.closeBrace);
            }

            case STATIC_VARIABLE -> {
                BoundStaticVariableNode variable = (BoundStaticVariableNode) entry.node;
                if (variable.type.isMissing()) {
                    yield variable.syntaxNode.keyword.getRange().isBefore(line, column);
                } else {
                    yield false;
                }
            }

            case FUNCTION -> {
                BoundFunctionNode functionNode = (BoundFunctionNode) entry.node;
                if (functionNode.syntaxNode.modifiers.getRange().isBefore(line, column)) {
                    if (functionNode.returnType.isMissing() || functionNode.returnType.getRange().getEnd().isAfter(line, column)) {
                        yield true;
                    }
                }
                yield false;
            }

            case META_TYPE_EXPRESSION -> {
                BoundMetaTypeExpressionNode meta = (BoundMetaTypeExpressionNode) entry.node;
                yield TextRange.isBetween(line, column, meta.syntaxNode.openParen, meta.syntaxNode.closeParen);
            }

            case PARAMETER_LIST -> {
                BoundParameterListNode parameters = (BoundParameterListNode) entry.node;
                if (TextRange.isBetween(line, column, parameters.syntaxNode.openParen, parameters.syntaxNode.closeParen)) {
                    if (parameters.parameters.isEmpty()) {
                        yield true;
                    }

                    if (TextRange.isBetween(line, column, parameters.syntaxNode.openParen, parameters.parameters.getFirst())) {
                        yield true;
                    }

                    for (int i = 1; i < parameters.parameters.size(); i++) {
                        if (TextRange.isBetween(line, column, parameters.parameters.get(i - 1), parameters.parameters.get(i))) {
                            yield true;
                        }
                    }

                    if (TextRange.isBetween(line, column, parameters.parameters.getFirst(), parameters.syntaxNode.closeParen)) {
                        yield true;
                    }
                }

                yield false;
            }

            case CLASS_DECLARATION -> canClassMember();

            case INVALID_TYPE, PREDEFINED_TYPE, CUSTOM_TYPE, DECLARED_CLASS_TYPE -> {
                yield entry.parent.node.isNot(BoundNodeType.CLASS_DECLARATION);
            }

            case INVALID_EXPRESSION -> {
                BoundInvalidExpressionNode invalidExpression = (BoundInvalidExpressionNode) entry.node;
                if (invalidExpression.syntaxNode != null) {
                    if (invalidExpression.syntaxNode.nodes.size() == 1 && invalidExpression.syntaxNode.nodes.getFirst() instanceof Token token) {
                        yield token.is(TokenType.NEW) && token.getRange().isBefore(line, column);
                    }
                    if (invalidExpression.syntaxNode.nodes.size() == 2 && invalidExpression.syntaxNode.nodes.getLast() instanceof CustomTypeNode custom) {
                        yield custom.getRange().containsOrEnds(line, column);
                    }
                }
                yield false;
            }

            default -> canStatement();
        };
    }

    private boolean canVoidInternal() {
        if (canUnitMember()) {
            return true;
        }

        if (entry == null) {
            return false;
        }

        return switch (entry.node.getNodeType()) {

            case FUNCTION -> {
                BoundFunctionNode functionNode = (BoundFunctionNode) entry.node;
                if (functionNode.syntaxNode.modifiers.getRange().isBefore(line, column)) {
                    if (functionNode.returnType.isMissing() || functionNode.returnType.getRange().getEnd().isAfter(line, column)) {
                        yield true;
                    }
                }
                yield false;
            }

            case CLASS_DECLARATION -> canClassMember();

            case EXTENSION_DECLARATION -> {
                BoundExtensionNode extension = (BoundExtensionNode) entry.node;
                yield TextRange.isBetween(line, column, extension.syntaxNode.openBrace, extension.syntaxNode.closeBrace);
            }

            default -> false;
        };
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