package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class MetaTypeExpressionNode extends ExpressionNode {

    public final TypeNode type;

    public MetaTypeExpressionNode(TypeNode type, TextRange range) {
        super(NodeType.META_TYPE_EXPRESSION, range);
        this.type = type;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaTypeExpressionNode other) {
            return other.type.equals(type) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}