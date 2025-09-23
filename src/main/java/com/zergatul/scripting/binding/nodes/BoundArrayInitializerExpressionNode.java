package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ArrayInitializerExpressionNode;

import java.util.ArrayList;
import java.util.List;

public class BoundArrayInitializerExpressionNode extends BoundExpressionNode {

    public final Token keyword;
    public final BoundTypeNode typeNode;
    public final Token openBrace;
    public final List<BoundExpressionNode> items;
    public final Token closeBrace;

    public BoundArrayInitializerExpressionNode(ArrayInitializerExpressionNode node, BoundTypeNode typeNode, List<BoundExpressionNode> items) {
        this(node.keyword, typeNode, node.openBrace, items, node.closeBrace, node.getRange());
    }

    public BoundArrayInitializerExpressionNode(Token keyword, BoundTypeNode typeNode, Token openBrace, List<BoundExpressionNode> items, Token closeBrace, TextRange range) {
        super(BoundNodeType.ARRAY_INITIALIZER_EXPRESSION, typeNode.type, range);
        this.keyword = keyword;
        this.typeNode = typeNode;
        this.openBrace = openBrace;
        this.items = items;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        typeNode.accept(visitor);
        for (BoundExpressionNode expression : items) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>();
        list.add(typeNode);
        list.addAll(items);
        return list;
    }
}