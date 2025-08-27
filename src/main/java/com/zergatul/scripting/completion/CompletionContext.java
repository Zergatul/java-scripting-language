package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class CompletionContext {

    public final ContextType type;
    public final SearchEntry entry;
    public final BoundNode prev;
    public final BoundNode next;
    public final int line;
    public final int column;

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
                return new CompletionContext(ContextType.BEFORE_FIRST, line, column);
            }
            if (unit.getRange().isBefore(line, column)) {
                if (unit.getRange().endsWith(line, column)) {
                    return getAtLastContext(unit, line, column);
                } else {
                    return new CompletionContext(ContextType.AFTER_LAST, line, column);
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

    public boolean canExpression() {
        return switch (entry.node.getNodeType()) {
            case IF_STATEMENT -> {
                BoundIfStatementNode statement = (BoundIfStatementNode) entry.node;
                // if (<cursor> <condition>
                yield TextRange.isBetween2(line, column, statement.lParen, statement.condition);
            }
            case NAME_EXPRESSION -> true;
            default -> canStatement();
        };
    }

    public boolean canStatement() {
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
                yield TextRange.isBetween(line, column, loop.rParen, loop.body);
            }
            default -> false;
        };
    }

    public CompletionContext closestStatement() {
        CompletionContext current = this;
        while (true) {
            if (current.entry.node instanceof BoundStatementNode) {
                return current;
            }
            if (current.entry.node.getNodeType() == NodeType.LAMBDA_EXPRESSION) {
                return null;
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
            return  statement.lParen.getRange().isEmpty() &&
                    statement.typeNode.getNodeType() == NodeType.INVALID_TYPE &&
                    statement.name.value.isEmpty() &&
                    statement.iterable.getNodeType() == NodeType.INVALID_EXPRESSION &&
                    statement.rParen.getRange().isEmpty() &&
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
            return new CompletionContext(ContextType.AFTER_LAST, line, column);
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