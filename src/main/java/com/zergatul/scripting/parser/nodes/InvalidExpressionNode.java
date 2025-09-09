package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class InvalidExpressionNode extends ExpressionNode {

    public final List<Locatable> nodes;

    public InvalidExpressionNode(List<Locatable> nodes) {
        this(nodes, TextRange.combine(nodes));
    }

    public InvalidExpressionNode(TextRange range) {
        this(List.of(), range);
    }

    public InvalidExpressionNode(List<Locatable> nodes, TextRange range) {
        super(ParserNodeType.INVALID_EXPRESSION, range);
        this.nodes = nodes;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.copyOf(nodes);
    }
}