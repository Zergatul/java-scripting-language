package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.AwaitExpressionNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundAwaitExpressionNode extends BoundExpressionNode {

    public final Token keyword;
    public final BoundExpressionNode expression;

    public BoundAwaitExpressionNode(AwaitExpressionNode node, BoundExpressionNode expression, SType type) {
        this(node.keyword, expression, type, node.getRange());
    }

    public BoundAwaitExpressionNode(Token keyword, BoundExpressionNode expression, SType type, TextRange range) {
        super(BoundNodeType.AWAIT_EXPRESSION, type, range);
        this.keyword = keyword;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}