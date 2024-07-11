package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundIfStatementNode extends BoundStatementNode {

    public final BoundExpressionNode condition;
    public final BoundStatementNode thenStatement;
    public final BoundStatementNode elseStatement;

    public BoundIfStatementNode(BoundExpressionNode condition, BoundStatementNode thenStatement, BoundStatementNode elseStatement, TextRange range) {
        super(NodeType.IF_STATEMENT, range);
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public boolean isAsync() {
        return condition.isAsync() || thenStatement.isAsync() || (elseStatement != null && elseStatement.isAsync());
    }

    @Override
    public List<BoundNode> getChildren() {
        return elseStatement == null ? List.of(condition, thenStatement) : List.of(condition, thenStatement, elseStatement);
    }
}