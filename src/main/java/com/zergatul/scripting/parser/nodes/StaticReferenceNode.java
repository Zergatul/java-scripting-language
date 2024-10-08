package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.PredefinedType;

public class StaticReferenceNode extends ExpressionNode {

    public final PredefinedType typeReference;

    public StaticReferenceNode(PredefinedType typeReference, TextRange range) {
        super(NodeType.STATIC_REFERENCE, range);
        this.typeReference = typeReference;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}