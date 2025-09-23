package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ArrayTypeNode;
import com.zergatul.scripting.type.SArrayType;

import java.util.List;

public class BoundArrayTypeNode extends BoundTypeNode {

    public final BoundTypeNode underlying;
    public final Token openBracket;
    public final Token closeBracket;

    public BoundArrayTypeNode(ArrayTypeNode node, BoundTypeNode underlying) {
        this(underlying, node.openBracket, node.closeBracket, node.getRange());
    }

    public BoundArrayTypeNode(BoundTypeNode underlying, Token openBracket, Token closeBracket, TextRange range) {
        super(BoundNodeType.ARRAY_TYPE, new SArrayType(underlying.type), range);
        this.underlying = underlying;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        underlying.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(underlying);
    }
}