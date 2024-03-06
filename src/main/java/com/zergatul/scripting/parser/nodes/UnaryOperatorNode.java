package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.UnaryOperator;

public class UnaryOperatorNode extends Node {

    public final UnaryOperator operator;

    public UnaryOperatorNode(UnaryOperator operator, TextRange range) {
        super(range);
        this.operator = operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnaryOperatorNode other) {
            return other.operator == operator && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}