package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class TypeTestExpressionNode extends ExpressionNode {

    public final ExpressionNode expression;
    public final PatternNode pattern;

    public TypeTestExpressionNode(ExpressionNode expression, PatternNode pattern, TextRange range) {
        super(NodeType.TYPE_TEST_EXPRESSION, range);
        this.expression = expression;
        this.pattern = pattern;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        expression.accept(visitor);
        pattern.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeTestExpressionNode other) {
            return  other.expression.equals(expression) &&
                    other.pattern.equals(pattern) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}