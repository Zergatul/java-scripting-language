package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class BinaryOperatorNode extends Node {

    public final BinaryOperator operator;

    public BinaryOperatorNode(BinaryOperator operator, TextRange range) {
        super(NodeType.BINARY_OPERATOR, range);
        this.operator = operator;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryOperatorNode other) {
            return other.operator == operator && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}