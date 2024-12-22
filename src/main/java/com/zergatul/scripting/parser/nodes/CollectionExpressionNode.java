package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class CollectionExpressionNode extends ExpressionNode {

    public final List<ExpressionNode> items;

    public CollectionExpressionNode(List<ExpressionNode> items, TextRange range) {
        super(NodeType.COLLECTION_EXPRESSION, range);
        this.items = items;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ExpressionNode item : items) {
            item.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CollectionExpressionNode other) {
            return  Objects.equals(other.items, items) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}