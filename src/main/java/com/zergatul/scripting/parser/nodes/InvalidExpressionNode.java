package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class InvalidExpressionNode extends ExpressionNode {

    public InvalidExpressionNode(TextRange range) {
        super(ParserNodeType.INVALID_EXPRESSION, range);
        if (!range.isEmpty()) {
            throw new InternalException();
        }
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.of();
    }
}