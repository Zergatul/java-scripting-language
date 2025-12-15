package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ConstantPatternNode extends PatternNode {

    public final ExpressionNode expression;

    public ConstantPatternNode(ExpressionNode expression) {
        super(ParserNodeType.CONSTANT_PATTERN, expression.getRange());
        this.expression = expression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(expression);
    }
}