package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.BinaryOperator;

public class BinaryOperatorNode extends Node {

    public final BinaryOperator operator;

    public BinaryOperatorNode(BinaryOperator operator, TextRange range) {
        super(range);
        this.operator = operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinaryOperatorNode other) {
            return other.operator == operator && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}