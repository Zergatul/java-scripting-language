package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.FallthroughFlow;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.IfStatementNode;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundIfStatementNode extends BoundStatementNode {

    public final IfStatementNode syntaxNode;
    public final BoundExpressionNode condition;
    public final BoundStatementNode thenStatement;
    public final @Nullable BoundStatementNode elseStatement;
    public final FallthroughFlow flow;

    public BoundIfStatementNode(
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            FallthroughFlow flow
    ) {
        this(SyntaxFactory.missingIfStatement(), condition, thenStatement, null, flow, TextRange.MISSING);
    }

    public BoundIfStatementNode(
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            @Nullable BoundStatementNode elseStatement,
            FallthroughFlow flow
    ) {
        this(SyntaxFactory.missingIfStatement(), condition, thenStatement, elseStatement, flow, TextRange.MISSING);
    }

    public BoundIfStatementNode(
            IfStatementNode node,
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            @Nullable BoundStatementNode elseStatement,
            FallthroughFlow flow
    ) {
        this(node, condition, thenStatement, elseStatement, flow, node.getRange());
    }

    public BoundIfStatementNode(
            IfStatementNode node,
            BoundExpressionNode condition,
            BoundStatementNode thenStatement,
            @Nullable BoundStatementNode elseStatement,
            FallthroughFlow flow,
            TextRange range
    ) {
        super(BoundNodeType.IF_STATEMENT, range);
        this.syntaxNode = node;
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
        this.flow = flow;
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