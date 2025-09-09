package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CharLiteralExpressionNode;
import com.zergatul.scripting.type.SChar;

import java.util.List;

public class BoundCharLiteralExpressionNode extends BoundExpressionNode {

    public final CharLiteralExpressionNode syntaxNode;
    public final char value;

    public BoundCharLiteralExpressionNode(CharLiteralExpressionNode node) {
        this(node, node.value, node.getRange());
    }

    public BoundCharLiteralExpressionNode(CharLiteralExpressionNode node, char value, TextRange range) {
        super(BoundNodeType.CHAR_LITERAL, SChar.instance, range);
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