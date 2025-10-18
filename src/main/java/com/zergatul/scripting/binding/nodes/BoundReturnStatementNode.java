package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.ReturnStatementNode;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundReturnStatementNode extends BoundStatementNode {

    public final ReturnStatementNode syntaxNode;
    @Nullable public final BoundExpressionNode expression;

    public BoundReturnStatementNode(BoundExpressionNode expression) {
        this(SyntaxFactory.missingReturnStatement(), expression, TextRange.MISSING);
    }

    public BoundReturnStatementNode(
            ReturnStatementNode node,
            @Nullable BoundExpressionNode expression
    ) {
        this(node, expression, node.getRange());
    }

    public BoundReturnStatementNode(
            ReturnStatementNode node,
            @Nullable BoundExpressionNode expression,
            TextRange range
    ) {
        super(BoundNodeType.RETURN_STATEMENT, range);
        this.syntaxNode = node;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return expression == null ? List.of() : List.of(expression);
    }
}