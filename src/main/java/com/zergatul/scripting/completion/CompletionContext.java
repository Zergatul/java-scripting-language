package com.zergatul.scripting.completion;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.PredefinedType;

import java.util.List;

public class CompletionContext {

    public final ContextType type;
    public final SearchEntry entry;
    public final BoundNode prev;
    public final BoundNode next;
    public final int line;
    public final int column;

    private final Lazy<Boolean> canUnitMemberLazy = new Lazy<>(this::canUnitMemberInternal);
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
            if (current.entry.node.getNodeType() == NodeType.FUNCTION) {
                return null;
            }
            if (current.entry.node.getNodeType() == NodeType.CLASS_METHOD) {
                return null;
            }
            if (current.entry.node.getNodeType() == NodeType.CLASS_CONSTRUCTOR) {
                return null;
            }
            current = current.up();
            if (current == null) {
                return null;
            }
        }
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
            return entry.node.getNodeType() == NodeType.COMPILATION_UNIT;
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

        if (type == ContextType.WITHIN && entry.node.getNodeType() == NodeType.COMPILATION_UNIT) {
            return true;
        }

        // handle cases like this:
        // i<cursor>
        if (isSingleWordStatementStart(entry, line, column)) {
            return true;
        }
        return switch (entry.node.getNodeType()) {
            case STATEMENTS_LIST, BLOCK_STATEMENT -> true;
            case FOR_LOOP_STATEMENT -> {
                BoundForLoopStatementNode loop = (BoundForLoopStatementNode) entry.node;
                yield TextRange.isBetween(line, column, loop.rParen, loop.body);
            }
            case FOREACH_LOOP_STATEMENT -> {
                BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) entry.node;
                yield TextRange.isBetween(line, column, loop.closeParen, loop.body);
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
            case STATIC_VARIABLE -> {
                BoundStaticVariableNode variableNode = (BoundStaticVariableNode) entry.node;
                if (variableNode.equal == null) {
                    yield false;
                } else {
                    yield variableNode.equal.getRange().isBefore(line, column);
                }
            }
            case IF_STATEMENT -> {
                BoundIfStatementNode statement = (BoundIfStatementNode) entry.node;
                // if (<cursor> <condition>
                yield TextRange.isBetween2(line, column, statement.lParen, statement.condition);
            }
            case ASSIGNMENT_STATEMENT -> {
                BoundAssignmentStatementNode statement = (BoundAssignmentStatementNode) entry.node;
                yield statement.operator.getRange().isBefore(line, column);
            }
            case LAMBDA_EXPRESSION -> {
                BoundLambdaExpressionNode lambda = (BoundLambdaExpressionNode) entry.node;
                yield lambda.isOpen() && lambda.arrow.getRange().isBefore(line, column);
            }
            case META_TYPE_OF_EXPRESSION -> {
                BoundMetaTypeOfExpressionNode meta = (BoundMetaTypeOfExpressionNode) entry.node;
                yield TextRange.isBetween(line, column, meta.openParen, meta.closeParen);
            }
            case NAME_EXPRESSION -> {
                yield switch (entry.parent.node.getNodeType()) {
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
            case STATIC_VARIABLE -> {
                BoundStaticVariableNode variable = (BoundStaticVariableNode) entry.node;
                if (variable.type.isMissing()) {
                    yield variable.keyword.getRange().isBefore(line, column);
                } else {
                    yield false;
                }
            }

            case FUNCTION -> {
                BoundFunctionNode functionNode = (BoundFunctionNode) entry.node;
                if (functionNode.modifiers.getRange().isBefore(line, column)) {
                    if (functionNode.returnType.isMissing() || functionNode.returnType.getRange().getEnd().isAfter(line, column)) {
                        yield true;
                    }
                }
                yield false;
            }

            case META_TYPE_EXPRESSION -> {
                BoundMetaTypeExpressionNode meta = (BoundMetaTypeExpressionNode) entry.node;
                yield TextRange.isBetween(line, column, meta.openParen, meta.closeParen);
            }

            case PARAMETER_LIST -> {
                BoundParameterListNode parameters = (BoundParameterListNode) entry.node;
                if (TextRange.isBetween(line, column, parameters.openParen, parameters.closeParen)) {
                    if (parameters.parameters.isEmpty()) {
                        yield true;
                    }

                    if (TextRange.isBetween(line, column, parameters.openParen, parameters.parameters.getFirst())) {
                        yield true;
                    }

                    for (int i = 1; i < parameters.parameters.size(); i++) {
                        if (TextRange.isBetween(line, column, parameters.parameters.get(i - 1), parameters.parameters.get(i))) {
                            yield true;
                        }
                    }

                    if (TextRange.isBetween(line, column, parameters.parameters.getFirst(), parameters.closeParen)) {
                        yield true;
                    }
                }

                yield false;
            }

            case CLASS -> true;

            case INVALID_TYPE, PREDEFINED_TYPE, CUSTOM_TYPE -> true;

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
                if (functionNode.modifiers.getRange().isBefore(line, column)) {
                    if (functionNode.returnType.isMissing() || functionNode.returnType.getRange().getEnd().isAfter(line, column)) {
                        yield true;
                    }
                }
                yield false;
            }

            case CLASS -> true;

            default -> false;
        };
    }

    private static boolean isSingleWordStatementStart(SearchEntry entry, int line, int column) {
        if (entry.node.getNodeType() == NodeType.NAME_EXPRESSION) {
            if (entry.parent.node.getNodeType() == NodeType.EXPRESSION_STATEMENT) {
                if (entry.node.getRange().containsOrEnds(line, column)) {
                    return true;
                }
            }
        }
        if (entry.node.getNodeType() == NodeType.PREDEFINED_TYPE) {
            if (entry.parent.node.getNodeType() == NodeType.VARIABLE_DECLARATION) {
                BoundVariableDeclarationNode declaration = (BoundVariableDeclarationNode) entry.parent.node;
                return declaration.name.value.isEmpty() && declaration.expression == null;
            }
        }
        if (entry.node.getNodeType() == NodeType.IF_STATEMENT) {
            BoundIfStatementNode statement = (BoundIfStatementNode) entry.node;
            return  statement.lParen.getRange().isEmpty() &&
                    statement.condition.getRange().isEmpty() &&
                    statement.rParen.getRange().isEmpty() &&
                    statement.thenStatement.getNodeType() == NodeType.INVALID_STATEMENT &&
                    statement.elseStatement == null;
        }
        if (entry.node.getNodeType() == NodeType.FOR_LOOP_STATEMENT) {
            BoundForLoopStatementNode statement = (BoundForLoopStatementNode) entry.node;
            return  statement.lParen.getRange().isEmpty() &&
                    statement.init.getNodeType() == NodeType.INVALID_STATEMENT &&
                    statement.condition.getRange().isEmpty() &&
                    statement.update.getNodeType() == NodeType.INVALID_STATEMENT &&
                    statement.rParen.getRange().isEmpty() &&
                    statement.body.getNodeType() == NodeType.INVALID_STATEMENT;
        }
        if (entry.node.getNodeType() == NodeType.FOREACH_LOOP_STATEMENT) {
            BoundForEachLoopStatementNode statement = (BoundForEachLoopStatementNode) entry.node;
            return  statement.openParen.getRange().isEmpty() &&
                    statement.typeNode.getNodeType() == NodeType.INVALID_TYPE &&
                    statement.name.value.isEmpty() &&
                    statement.iterable.getNodeType() == NodeType.INVALID_EXPRESSION &&
                    statement.closeParen.getRange().isEmpty() &&
                    statement.body.getNodeType() == NodeType.INVALID_STATEMENT;
        }
        if (entry.node.getNodeType() == NodeType.WHILE_LOOP_STATEMENT) {
            BoundWhileLoopStatementNode statement = (BoundWhileLoopStatementNode) entry.node;
            return  statement.condition.getRange().isEmpty() &&
                    statement.body.getNodeType() == NodeType.INVALID_STATEMENT;
        }
        if (entry.node.getNodeType() == NodeType.RETURN_STATEMENT) {
            BoundReturnStatementNode statement = (BoundReturnStatementNode) entry.node;
            return  statement.keyword.getRange().containsOrEnds(line, column) &&
                    (statement.expression == null || statement.expression.getNodeType() == NodeType.INVALID_EXPRESSION);
        }
        return false;
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