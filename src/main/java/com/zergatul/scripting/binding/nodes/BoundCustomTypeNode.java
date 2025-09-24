package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundCustomTypeNode extends BoundTypeNode {

    public final Token token;

    public BoundCustomTypeNode(Token token, SType type, TextRange range) {
        super(BoundNodeType.CUSTOM_TYPE, type, range);
        this.token = token;
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