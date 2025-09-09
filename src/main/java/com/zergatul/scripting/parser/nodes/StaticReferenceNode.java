package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class StaticReferenceNode extends ExpressionNode {

    public final TypeNode typeNode;

    public StaticReferenceNode(TypeNode typeNode, TextRange range) {
        super(ParserNodeType.STATIC_REFERENCE, range);
        this.typeNode = typeNode;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(typeNode);
    }
}