package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.PredefinedType;

public class StaticReferenceNode extends ExpressionNode {

    public final TypeNode typeNode;

    public StaticReferenceNode(TypeNode typeNode, TextRange range) {
        super(NodeType.STATIC_REFERENCE, range);
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
}