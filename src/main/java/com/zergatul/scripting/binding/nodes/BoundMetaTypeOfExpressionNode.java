package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.nodes.MetaTypeOfExpressionNode;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaTypeOfExpressionNode extends BoundExpressionNode {

    public final Token keyword;
    public final Token openParen;
    public final BoundExpressionNode expression;
    public final Token closeParen;

    public BoundMetaTypeOfExpressionNode(BoundExpressionNode expression, MetaTypeOfExpressionNode node) {
        this(node.keyword, node.openParen, expression, node.closeParen, node.getRange());
    }

    public BoundMetaTypeOfExpressionNode(Token keyword, Token openParen, BoundExpressionNode expression, Token closeParen, TextRange range) {
        super(NodeType.META_TYPE_OF_EXPRESSION, SType.fromJavaType(RuntimeType.class), range);
        this.keyword = keyword;
        this.openParen = openParen;
        this.expression = expression;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}