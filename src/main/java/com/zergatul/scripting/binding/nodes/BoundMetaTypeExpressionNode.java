package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.MetaTypeExpressionNode;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundMetaTypeExpressionNode extends BoundExpressionNode {

    public final Token keyword;
    public final Token openParen;
    public final BoundTypeNode type;
    public final Token closeParen;

    public BoundMetaTypeExpressionNode(BoundTypeNode type, MetaTypeExpressionNode node) {
        this(node.keyword, node.openParen, type, node.closeParen, node.getRange());
    }

    public BoundMetaTypeExpressionNode(Token keyword, Token openParen, BoundTypeNode type, Token closeParen, TextRange range) {
        super(BoundNodeType.META_TYPE_EXPRESSION, SType.fromJavaType(RuntimeType.class), range);
        this.keyword = keyword;
        this.openParen = openParen;
        this.type = type;
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
        return List.of(type);
    }
}