package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.WhileLoopStatementNode;

import java.util.List;

public class BoundWhileLoopStatementNode extends BoundStatementNode {

    public final WhileLoopStatementNode syntaxNode;
    public final BoundExpressionNode condition;
    public final BoundStatementNode body;

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body) {
        this(SyntaxFactory.missingWhileLoopStatement(), condition, body, TextRange.MISSING);
    }

    public BoundWhileLoopStatementNode(BoundExpressionNode condition, BoundStatementNode body, WhileLoopStatementNode node) {
        this(node, condition, body, node.getRange());
    }

    public BoundWhileLoopStatementNode(WhileLoopStatementNode node, BoundExpressionNode condition, BoundStatementNode body, TextRange range) {
        super(BoundNodeType.WHILE_LOOP_STATEMENT, range);
        this.syntaxNode = node;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(condition, body);
    }
}