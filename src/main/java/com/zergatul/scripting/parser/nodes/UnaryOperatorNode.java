package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.UnaryOperator;

public class UnaryOperatorNode extends ParserNode {

    public final UnaryOperator operator;

    public UnaryOperatorNode(UnaryOperator operator, TextRange range) {
        super(NodeType.UNARY_OPERATOR, range);
        this.operator = operator;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnaryOperatorNode other) {
            return other.operator == operator && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}