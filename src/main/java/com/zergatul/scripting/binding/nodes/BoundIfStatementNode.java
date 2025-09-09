package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.IfStatementNode;

import java.util.List;

public class BoundIfStatementNode extends BoundStatementNode {

    public final IfStatementNode syntaxNode;
    public final BoundExpressionNode condition;
    public final BoundStatementNode thenStatement;
    public final BoundStatementNode elseStatement;

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement) {
        this(null, condition, thenStatement, null, null);
    }

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement) {
        this(null, condition, thenStatement, elseStatement, null);
    }

    public BoundIfStatementNode(IfStatementNode node, BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement) {
        this(node, condition, thenStatement, elseStatement, node.getRange());
    }

    public BoundIfStatementNode(
            IfStatementNode node,
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            BoundStatementNode elseStatement,
            TextRange range
    ) {
        super(BoundNodeType.IF_STATEMENT, range);
        this.syntaxNode = node;
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        thenStatement.accept(visitor);
        if (elseStatement != null) {
            elseStatement.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return elseStatement == null ? List.of(condition, thenStatement) : List.of(condition, thenStatement, elseStatement);
    }
}