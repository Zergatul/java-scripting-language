package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.nodes.*;

public class SearchEntry {

    public final SearchEntry parent;
    public final BoundNode node;

    public SearchEntry(SearchEntry parent, BoundNode node) {
        this.parent = parent;
        this.node = node;
    }

    public boolean isSingleWordStatementStart(int line, int column) {
        if (node.getNodeType() == BoundNodeType.NAME_EXPRESSION) {
            if (parent.node.getNodeType() == BoundNodeType.EXPRESSION_STATEMENT) {
                if (node.getRange().containsOrEnds(line, column)) {
                    return true;
                }
            }
        }
        if (node.getNodeType() == BoundNodeType.PREDEFINED_TYPE) {
            if (parent.node.getNodeType() == BoundNodeType.VARIABLE_DECLARATION) {
                BoundVariableDeclarationNode declaration = (BoundVariableDeclarationNode) parent.node;
                return declaration.name.value.isEmpty() && declaration.expression == null;
            }
        }
        if (node.getNodeType() == BoundNodeType.IF_STATEMENT) {
            BoundIfStatementNode statement = (BoundIfStatementNode) node;
            return  statement.syntaxNode.openParen.getRange().isEmpty() &&
                    statement.condition.getRange().isEmpty() &&
                    statement.syntaxNode.closeParen.getRange().isEmpty() &&
                    statement.thenStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT &&
                    statement.elseStatement == null;
        }
        if (node.getNodeType() == BoundNodeType.FOR_LOOP_STATEMENT) {
            BoundForLoopStatementNode statement = (BoundForLoopStatementNode) node;
            return  statement.syntaxNode.openParen.getRange().isEmpty() &&
                    statement.init.getNodeType() == BoundNodeType.INVALID_STATEMENT &&
                    statement.condition.getRange().isEmpty() &&
                    statement.update.getNodeType() == BoundNodeType.INVALID_STATEMENT &&
                    statement.syntaxNode.closeParen.getRange().isEmpty() &&
                    statement.body.getNodeType() == BoundNodeType.INVALID_STATEMENT;
        }
        if (node.getNodeType() == BoundNodeType.FOREACH_LOOP_STATEMENT) {
            BoundForEachLoopStatementNode statement = (BoundForEachLoopStatementNode) node;
            return  statement.syntaxNode.openParen.getRange().isEmpty() &&
                    statement.typeNode.getNodeType() == BoundNodeType.INVALID_TYPE &&
                    statement.name.value.isEmpty() &&
                    statement.iterable.getNodeType() == BoundNodeType.INVALID_EXPRESSION &&
                    statement.syntaxNode.closeParen.getRange().isEmpty() &&
                    statement.body.getNodeType() == BoundNodeType.INVALID_STATEMENT;
        }
        if (node.getNodeType() == BoundNodeType.WHILE_LOOP_STATEMENT) {
            BoundWhileLoopStatementNode statement = (BoundWhileLoopStatementNode) node;
            return  statement.condition.getRange().isEmpty() &&
                    statement.body.getNodeType() == BoundNodeType.INVALID_STATEMENT;
        }
        if (node.getNodeType() == BoundNodeType.RETURN_STATEMENT) {
            BoundReturnStatementNode statement = (BoundReturnStatementNode) node;
            return  statement.syntaxNode.keyword.getRange().containsOrEnds(line, column) &&
                    (statement.expression == null || statement.expression.getNodeType() == BoundNodeType.INVALID_EXPRESSION);
        }
        return false;
    }
}