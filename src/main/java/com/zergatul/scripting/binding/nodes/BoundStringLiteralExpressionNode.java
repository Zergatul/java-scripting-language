package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.StringLiteralExpressionNode;
import com.zergatul.scripting.type.SString;

import java.util.List;

public class BoundStringLiteralExpressionNode extends BoundExpressionNode {

    public final StringLiteralExpressionNode syntaxNode;
    public final String value;

    public BoundStringLiteralExpressionNode(StringLiteralExpressionNode node, String value) {
        this(node, value, node.getRange());
    }

    public BoundStringLiteralExpressionNode(StringLiteralExpressionNode node, String value, TextRange range) {
        super(BoundNodeType.STRING_LITERAL, SString.instance, range);
        this.syntaxNode = node;
        this.value = value;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}